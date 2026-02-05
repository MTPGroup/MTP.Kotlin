package tech.hanasaki.azusa.modules.knowledge.domain.model

import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.modules.knowledge.domain.events.FileProcessed
import tech.hanasaki.azusa.modules.knowledge.domain.events.FileProcessingFailed
import tech.hanasaki.azusa.modules.knowledge.domain.events.FileUploaded
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * 知识文件实体
 */
data class KnowledgeFile(
    val id: KnowledgeFileId,
    val knowledgeBaseId: KnowledgeBaseId,
    val filePath: String,
    val fileName: String,
    val fileSize: Long?,
    val fileType: String?,
    var status: FileStatus,
    var errorMessage: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf(),
) {
    val domainEvents: List<DomainEvent> get() = _domainEvents

    fun clearDomainEvents() {
        _domainEvents.clear()
    }

    private fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    companion object {
        /**
         * 创建新文件记录
         */
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            filePath: String,
            fileName: String,
            fileSize: Long?,
            fileType: String?,
        ): KnowledgeFile {
            val now = Clock.System.now()
            val file = KnowledgeFile(
                id = KnowledgeFileId(Uuid.random()),
                knowledgeBaseId = knowledgeBaseId,
                filePath = filePath,
                fileName = fileName,
                fileSize = fileSize,
                fileType = fileType,
                status = FileStatus.PENDING,
                errorMessage = null,
                createdAt = now,
                updatedAt = now,
            )
            file.addDomainEvent(
                FileUploaded(
                    fileId = file.id,
                    knowledgeBaseId = knowledgeBaseId,
                    fileName = fileName,
                )
            )
            return file
        }

        /**
         * 从持久化层重建
         */
        fun reconstitute(
            id: KnowledgeFileId,
            knowledgeBaseId: KnowledgeBaseId,
            filePath: String,
            fileName: String,
            fileSize: Long?,
            fileType: String?,
            status: FileStatus,
            errorMessage: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): KnowledgeFile = KnowledgeFile(
            id = id,
            knowledgeBaseId = knowledgeBaseId,
            filePath = filePath,
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType,
            status = status,
            errorMessage = errorMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 标记为处理中
     */
    fun markProcessing() {
        status = FileStatus.PROCESSING
        updatedAt = Clock.System.now()
    }

    /**
     * 标记为完成
     */
    fun markCompleted() {
        status = FileStatus.COMPLETED
        errorMessage = null
        updatedAt = Clock.System.now()
        addDomainEvent(
            FileProcessed(
                fileId = id,
                knowledgeBaseId = knowledgeBaseId,
            )
        )
    }

    /**
     * 标记为失败
     */
    fun markFailed(error: String) {
        status = FileStatus.FAILED
        errorMessage = error
        updatedAt = Clock.System.now()
        addDomainEvent(
            FileProcessingFailed(
                fileId = id,
                knowledgeBaseId = knowledgeBaseId,
                errorMessage = error,
            )
        )
    }

    /**
     * 重置为待处理状态
     */
    fun resetToPending() {
        status = FileStatus.PENDING
        errorMessage = null
        updatedAt = Clock.System.now()
    }
}
