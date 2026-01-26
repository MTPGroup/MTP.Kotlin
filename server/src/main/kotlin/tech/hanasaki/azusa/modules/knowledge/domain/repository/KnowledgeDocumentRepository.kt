package tech.hanasaki.azusa.modules.knowledge.domain.repository

import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeDocument
import tech.hanasaki.azusa.common.kernel.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeDocumentId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeFileId

interface KnowledgeDocumentRepository {
    suspend fun findById(id: KnowledgeDocumentId): KnowledgeDocument?
    suspend fun save(document: KnowledgeDocument)
    suspend fun saveAll(documents: List<KnowledgeDocument>)
    suspend fun deleteById(id: KnowledgeDocumentId)

    /**
     * 查询知识库的所有文档
     */
    suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeDocument>

    /**
     * 查询文件的所有文档
     */
    suspend fun findByFileId(fileId: KnowledgeFileId): List<KnowledgeDocument>

    /**
     * 删除知识库的所有文档
     */
    suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId)

    /**
     * 删除文件的所有文档
     */
    suspend fun deleteByFileId(fileId: KnowledgeFileId)

    /**
     * 统计知识库的文档数量
     */
    suspend fun countByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): Long
}
