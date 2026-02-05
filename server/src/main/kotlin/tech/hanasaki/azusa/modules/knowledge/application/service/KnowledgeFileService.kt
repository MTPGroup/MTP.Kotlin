package tech.hanasaki.azusa.modules.knowledge.application.service

import tech.hanasaki.azusa.common.domain.exception.AuthorizationException
import tech.hanasaki.azusa.common.domain.exception.NotFoundException
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.common.port.out.EventPublisher
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeDocument
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.modules.knowledge.domain.port.DocumentParser
import tech.hanasaki.azusa.modules.knowledge.domain.port.EmbeddingService
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeFileRepository

class KnowledgeFileService(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val fileRepository: KnowledgeFileRepository,
    private val documentRepository: KnowledgeDocumentRepository,
    private val documentParser: DocumentParser,
    private val embeddingService: EmbeddingService,
    private val eventPublisher: EventPublisher,
) {

    /**
     * 上传文件到知识库
     */
    suspend fun uploadFile(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        fileName: String,
        filePath: String,
        fileSize: Long?,
        fileType: String?,
    ): KnowledgeFile {
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
        eventPublisher.publishAll(file.domainEvents)
        file.clearDomainEvents()
        return file
    }

    /**
     * 获取知识库的文件列表
     */
    suspend fun listFiles(userId: UserId, knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile> {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        return fileRepository.findByKnowledgeBaseId(knowledgeBaseId)
    }

    /**
     * 获取文件详情
     */
    suspend fun getFile(userId: UserId, fileId: KnowledgeFileId): KnowledgeFile {
        val file = fileRepository.findById(fileId)
            ?: throw NotFoundException("File not found")
        val kb = knowledgeBaseRepository.findById(file.knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        return file
    }

    /**
     * 删除文件
     */
    suspend fun deleteFile(userId: UserId, fileId: KnowledgeFileId) {
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
    suspend fun processPendingFiles(limit: Int = 10): Int {
        val pendingFiles = fileRepository.findByStatus(FileStatus.PENDING, limit)
        var processedCount = 0

        for (file in pendingFiles) {
            try {
                processFile(file)
                processedCount++
            } catch (e: Exception) {
                file.markFailed(e.message ?: "Unknown error")
                fileRepository.save(file)
                eventPublisher.publishAll(file.domainEvents)
                file.clearDomainEvents()
            }
        }
        return processedCount
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
        eventPublisher.publishAll(file.domainEvents)
        file.clearDomainEvents()
    }

    /**
     * 重新处理失败的文件
     */
    suspend fun retryFailedFile(userId: UserId, fileId: KnowledgeFileId) {
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
