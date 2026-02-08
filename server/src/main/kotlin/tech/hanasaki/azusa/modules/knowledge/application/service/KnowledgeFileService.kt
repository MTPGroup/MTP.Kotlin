package tech.hanasaki.azusa.modules.knowledge.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParser
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort
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
import kotlinx.coroutines.delay

private val logger = KotlinLogging.logger {}

class KnowledgeFileService(
    private val knowledgeBaseRepository: KnowledgeBaseRepositoryPort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val documentRepository: KnowledgeDocumentRepositoryPort,
    private val documentParser: DocumentParser,
    private val embeddingService: EmbeddingServicePort,
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

    private suspend fun embedWithRetry(text: String, maxRetries: Int = 3): FloatArray {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return embeddingService.embed(text)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val delayMs = 1000L.shl(attempt)
                    logger.warn { "Embedding 失败（${attempt + 1}/$maxRetries），${delayMs}ms 后重试: ${e.message}" }
                    delay(delayMs)
                }
            }
        }
        
        throw lastException ?: Exception("Embedding 失败，已重试 $maxRetries 次")
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

            // 解析文件内容为文档块
            val chunks = documentParser.parse(file.filePath, file.fileType)

            // 为每个块生成 embedding 并保存
            val documents = chunks.map { chunk ->
                val embedding = embedWithRetry(chunk.content)
                KnowledgeDocument.create(
                    knowledgeBaseId = file.knowledgeBaseId,
                    fileId = file.id,
                    content = chunk.content,
                    metadata = chunk.metadata,
                    embedding = embedding,
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
