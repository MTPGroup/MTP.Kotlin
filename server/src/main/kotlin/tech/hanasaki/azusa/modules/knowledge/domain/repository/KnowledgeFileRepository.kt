package tech.hanasaki.azusa.modules.knowledge.domain.repository

import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId

interface KnowledgeFileRepository {
    suspend fun findById(id: KnowledgeFileId): KnowledgeFile?
    suspend fun save(file: KnowledgeFile)
    suspend fun deleteById(id: KnowledgeFileId)

    /**
     * 查询知识库的所有文件
     */
    suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile>

    /**
     * 查询待处理的文件
     */
    suspend fun findByStatus(status: FileStatus, limit: Int = 10): List<KnowledgeFile>

    /**
     * 删除知识库的所有文件
     */
    suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId)
}
