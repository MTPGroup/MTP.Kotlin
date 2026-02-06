package tech.hanasaki.azusa.modules.knowledge.domain.repository

import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase

interface KnowledgeBaseRepository {
    suspend fun findById(id: KnowledgeBaseId): KnowledgeBase?
    suspend fun save(knowledgeBase: KnowledgeBase)
    suspend fun deleteById(id: KnowledgeBaseId)

    /**
     * 查询用户的知识库（不分页）
     */
    suspend fun findByAuthorId(authorId: UserId): List<KnowledgeBase>

    /**
     * 查询用户的知识库（分页）
     */
    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<KnowledgeBase>

    /**
     * 查询公开的知识库（不分页）
     */
    suspend fun findPublic(): List<KnowledgeBase>

    /**
     * 查询公开的知识库（分页）
     */
    suspend fun findPublicPaged(page: Int, limit: Int): PageResult<KnowledgeBase>

    /**
     * 搜索公开的知识库
     */
    suspend fun searchPublic(query: String, page: Int, limit: Int): PageResult<KnowledgeBase>
}
