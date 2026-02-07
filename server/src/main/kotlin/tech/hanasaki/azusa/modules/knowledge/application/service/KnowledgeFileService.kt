package tech.hanasaki.azusa.modules.knowledge.application.service

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
        fileName: String,
        filePath: String,
        fileSize: Long?,
        fileType: String?,
    ): KnowledgeFile = tx.execute {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }

        val file = KnowledgeFile.create(
            knowledgeBaseId = knowledgeBaseId,
            filePath = filePath,
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
                ?: throw NotFoundException("Knowledge base not found")
            if (kb.authorId != userId) {
                throw AuthorizationException("Access denied")
            }
            fileRepository.findByKnowledgeBaseId(knowledgeBaseId)
        }

    override suspend fun getFile(userId: UserId, fileId: KnowledgeFileId): KnowledgeFile = tx.readOnly {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("File not found")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        file
    }

    override suspend fun deleteFile(userId: UserId, fileId: KnowledgeFileId) = tx.execute {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("File not found")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        // 删除关联的文档
        documentRepository.deleteByFileId(fileId)
        fileRepository.deleteById(fileId)
    }

    /**
     * 处理待处理的文件（由后台任务调用）
     */
    override suspend fun processPendingFiles(limit: Int): Int = tx.execute {
        val pendingFiles = fileRepository.findByStatus(FileStatus.PENDING, limit)
        var processedCount = 0

        for (file in pendingFiles) {
            try {
                processFile(file)
                processedCount++
            } catch (e: Exception) {
                file.markFailed(e.message ?: "Unknown error")
                fileRepository.save(file)
                file.publishAndClear(domainEventBus)
            }
        }
        processedCount
    }

    /**
     * 处理单个文件
     */
    private suspend fun processFile(file: KnowledgeFile) {
        file.markProcessing()
        fileRepository.save(file)

        // 解析文件内容为文档块
        val chunks = documentParser.parse(file.filePath, file.fileType)

        // 为每个块生成 embedding 并保存
        val documents = chunks.map { chunk ->
            val embedding = embeddingService.embed(chunk.content)
            KnowledgeDocument.create(
                knowledgeBaseId = file.knowledgeBaseId,
                fileId = file.id,
                content = chunk.content,
                metadata = chunk.metadata,
                embedding = embedding,
            )
        }
        documentRepository.saveAll(documents)

        // 标记文件处理完成
        file.markCompleted()
        fileRepository.save(file)
        file.publishAndClear(domainEventBus)
    }

    override suspend fun retryFailedFile(userId: UserId, fileId: KnowledgeFileId) = tx.execute {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("File not found")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        if (file.status != FileStatus.FAILED) {
            throw IllegalStateException("Only failed files can be retried")
        }

        // 删除之前可能部分处理的文档
        documentRepository.deleteByFileId(fileId)

        // 重置状态为待处理
        file.resetToPending()
        fileRepository.save(file)
    }
}
