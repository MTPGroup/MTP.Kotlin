package tech.hanasaki.azusa.modules.knowledge.application.port.`in`

import tech.hanasaki.azusa.modules.knowledge.application.port.out.SearchResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface KnowledgeSearchUseCasePort {
    suspend fun search(
        userId: UserId,
        knowledgeBaseIds: List<KnowledgeBaseId>,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult>

    suspend fun searchKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult>

    suspend fun searchMyKnowledgeBases(
        userId: UserId,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult>

    suspend fun searchPublicKnowledgeBases(
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult>
}
