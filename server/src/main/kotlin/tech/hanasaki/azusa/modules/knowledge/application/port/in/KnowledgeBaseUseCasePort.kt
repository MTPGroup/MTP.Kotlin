package tech.hanasaki.azusa.modules.knowledge.application.port.`in`

import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseStats
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface KnowledgeBaseUseCasePort {
    suspend fun listPublicKnowledgeBases(page: Int = 1, limit: Int = 10): PageResult<KnowledgeBase>
    suspend fun searchKnowledgeBases(query: String, page: Int = 1, limit: Int = 10): PageResult<KnowledgeBase>
    suspend fun listMyKnowledgeBases(userId: UserId, page: Int = 1, limit: Int = 10): PageResult<KnowledgeBase>
    suspend fun getKnowledgeBase(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBase
    suspend fun createKnowledgeBase(
        userId: UserId,
        name: String,
        description: String? = null,
        isPublic: Boolean = false,
    ): KnowledgeBase

    suspend fun updateKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        name: String,
        description: String?,
        isPublic: Boolean,
    ): KnowledgeBase

    suspend fun deleteKnowledgeBase(userId: UserId, knowledgeBaseId: KnowledgeBaseId)
    suspend fun getKnowledgeBaseStats(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBaseStats
}
