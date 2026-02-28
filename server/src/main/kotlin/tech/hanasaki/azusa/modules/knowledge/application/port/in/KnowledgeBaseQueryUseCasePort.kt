package tech.hanasaki.azusa.modules.knowledge.application.port.`in`

import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseView
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface KnowledgeBaseQueryUseCasePort {
    suspend fun listPublicKnowledgeBases(page: Int = 1, limit: Int = 10): PageResult<KnowledgeBaseView>
    suspend fun searchKnowledgeBases(query: String, page: Int = 1, limit: Int = 10): PageResult<KnowledgeBaseView>
    suspend fun listMyKnowledgeBases(userId: UserId, page: Int = 1, limit: Int = 10): PageResult<KnowledgeBaseView>
    suspend fun getKnowledgeBase(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBaseView
}
