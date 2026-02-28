package tech.hanasaki.azusa.modules.knowledge.application.service

import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseQueryUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseView
import tech.hanasaki.azusa.modules.knowledge.application.port.out.KnowledgeBaseQueryRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class KnowledgeBaseQueryService(
    private val knowledgeBaseQueryRepository: KnowledgeBaseQueryRepositoryPort,
    private val knowledgeBaseRepository: KnowledgeBaseRepositoryPort,
    private val tx: TransactionalPort,
) : KnowledgeBaseQueryUseCasePort {

    override suspend fun listPublicKnowledgeBases(page: Int, limit: Int): PageResult<KnowledgeBaseView> = tx.readOnly {
        knowledgeBaseQueryRepository.findPublicPaged(page, limit)
    }

    override suspend fun searchKnowledgeBases(query: String, page: Int, limit: Int): PageResult<KnowledgeBaseView> = tx.readOnly {
        knowledgeBaseQueryRepository.searchPublic(query, page, limit)
    }

    override suspend fun listMyKnowledgeBases(userId: UserId, page: Int, limit: Int): PageResult<KnowledgeBaseView> = tx.readOnly {
        knowledgeBaseQueryRepository.findByAuthorIdPaged(userId, page, limit)
    }

    override suspend fun getKnowledgeBase(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBaseView = tx.readOnly {
        knowledgeBaseQueryRepository.findVisibleById(knowledgeBaseId, userId)
            ?: run {
                val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
                    ?: throw NotFoundException("知识库不存在")
                if (!kb.isPublic && kb.authorId != userId) {
                    throw AuthorizationException("无权访问")
                }
                throw NotFoundException("知识库不存在")
            }
    }
}
