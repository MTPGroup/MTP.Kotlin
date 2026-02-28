package tech.hanasaki.azusa.modules.knowledge.application.port.out

import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseView
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface KnowledgeBaseQueryRepositoryPort {
    suspend fun findPublicPaged(page: Int, limit: Int): PageResult<KnowledgeBaseView>
    suspend fun searchPublic(query: String, page: Int, limit: Int): PageResult<KnowledgeBaseView>
    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<KnowledgeBaseView>
    suspend fun findVisibleById(knowledgeBaseId: KnowledgeBaseId, userId: UserId?): KnowledgeBaseView?
}
