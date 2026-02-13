package tech.hanasaki.azusa.modules.knowledge.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentIngestorPort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParserPort
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeDocument
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeDocumentRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

private val logger = KotlinLogging.logger { }

class KnowledgeFileService(
    private val knowledgeBaseRepository: KnowledgeBaseRepositoryPort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val documentRepository: KnowledgeDocumentRepositoryPort,
    private val documentParser: DocumentParserPort,
    private val documentIngestor: DocumentIngestorPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : KnowledgeFileUseCasePort {

    override suspend fun uploadFile(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        objectKey: String,
        fileName: String,
        fileSize: Long?,
        fileType: String?,
    ): KnowledgeFile = tx.execute {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }

        val file = KnowledgeFile.create(
            knowledgeBaseId = knowledgeBaseId,
            filePath = objectKey,
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType,
        )
        fileRepository.save(file)
        file.publishAndClear(domainEventBus)
        file
    }

    override suspend fun listFiles(userId: UserId, knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile> =
        tx.readOnly {
            val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
                ?: throw NotFoundException("知识库不存在")
            if (kb.authorId != userId) {
                throw AuthorizationException("无权访问")
            }
            fileRepository.findByKnowledgeBaseId(knowledgeBaseId)
        }

    override suspend fun getFile(userId: UserId, fileId: KnowledgeFileId): KnowledgeFile = tx.readOnly {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("文件不存在")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }
        file
    }

    override suspend fun deleteFile(userId: UserId, fileId: KnowledgeFileId) = tx.execute {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("文件不存在")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }
        // 删除关联的文档
        documentRepository.deleteByFileId(fileId)
        fileRepository.deleteById(fileId)
    }

    override suspend fun processFile(fileId: KnowledgeFileId) {
        try {
            val file = tx.execute {
                val f = fileRepository.findById(fileId)
                    ?: throw NotFoundException("文件不存在")
                f.markProcessing()
                fileRepository.save(f)
                f
            }

            // 解析文件内容为文本块
            val chunks = documentParser.parse(file.filePath, file.fileType)

            // 合并所有块的内容
            val fullContent = chunks.joinToString("\n\n") { it.content }

            // 使用 LangChain4j Ingestor 自动完成: 分割 -> Embedding -> 存储
            documentIngestor.ingest(
                content = fullContent,
                knowledgeBaseId = file.knowledgeBaseId,
                fileId = file.id,
                filePath = file.filePath,
                extraMetadata = chunks.firstOrNull()?.metadata ?: JsonObject(emptyMap())
            )

            // 同时保存原始文档记录到领域模型（保持元数据一致性）
            val documents = chunks.map { chunk ->
                KnowledgeDocument.create(
                    knowledgeBaseId = file.knowledgeBaseId,
                    fileId = file.id,
                    content = chunk.content,
                    metadata = chunk.metadata,
                    embedding = FloatArray(0), // Ingestor 会自动生成并存储，这里占位
                )
            }

            tx.execute {
                documentRepository.saveAll(documents)
                file.markCompleted()
                fileRepository.save(file)
                file.publishAndClear(domainEventBus)
            }
        } catch (e: Exception) {
            logger.error(e) { "处理文件失败 $fileId" }
            try {
                tx.execute {
                    val file = fileRepository.findById(fileId) ?: return@execute
                    file.markFailed(e.message ?: "未知错误")
                    fileRepository.save(file)
                    file.publishAndClear(domainEventBus)
                }
            } catch (inner: Exception) {
                logger.error(inner) { "标记文件 $fileId 为失败状态失败" }
            }
        }
    }

    override suspend fun retryFailedFile(userId: UserId, fileId: KnowledgeFileId) = tx.execute {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("文件不存在")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }
        if (file.status != FileStatus.FAILED) {
            throw IllegalStateException("只有失败的文件才能重试")
        }

        // 删除之前可能部分处理的文档
        documentRepository.deleteByFileId(fileId)

        // 重置状态为待处理
        file.resetToPending()
        fileRepository.save(file)
    }
}
