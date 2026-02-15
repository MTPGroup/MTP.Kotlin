package tech.hanasaki.azusa.modules.knowledge.application.port.`in`

import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface KnowledgeFileUseCasePort {
    suspend fun uploadFile(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        objectKey: String,
        fileName: String,
        fileSize: Long?,
        fileType: String?,
    ): KnowledgeFile

    suspend fun listFiles(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile>
    suspend fun getFile(userId: UserId, fileId: KnowledgeFileId): KnowledgeFile
    suspend fun deleteFile(userId: UserId, fileId: KnowledgeFileId)
    suspend fun processFile(fileId: KnowledgeFileId)
    suspend fun retryFailedFile(userId: UserId, fileId: KnowledgeFileId)
}
