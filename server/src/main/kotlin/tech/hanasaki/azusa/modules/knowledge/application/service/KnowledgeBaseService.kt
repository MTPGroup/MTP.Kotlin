package tech.hanasaki.azusa.modules.knowledge.application.service

import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseStats
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeDocumentRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class KnowledgeBaseService(
    private val knowledgeBaseRepository: KnowledgeBaseRepositoryPort,
    private val fileRepository: KnowledgeFileRepositoryPort,
    private val documentRepository: KnowledgeDocumentRepositoryPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : KnowledgeBaseUseCasePort {

    override suspend fun listPublicKnowledgeBases(page: Int, limit: Int): PageResult<KnowledgeBase> = tx.readOnly {
        knowledgeBaseRepository.findPublicPaged(page, limit)
    }

    override suspend fun searchKnowledgeBases(query: String, page: Int, limit: Int): PageResult<KnowledgeBase> =
        tx.readOnly {
            knowledgeBaseRepository.searchPublic(query, page, limit)
        }

    override suspend fun listMyKnowledgeBases(userId: UserId, page: Int, limit: Int): PageResult<KnowledgeBase> =
        tx.readOnly {
            knowledgeBaseRepository.findByAuthorIdPaged(userId, page, limit)
        }

    override suspend fun getKnowledgeBase(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBase =
        tx.readOnly {
            val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
                ?: throw NotFoundException("知识库不存在")
            if (!kb.isPublic && kb.authorId != userId) {
                throw AuthorizationException("无权访问")
            }
            kb
        }

    override suspend fun createKnowledgeBase(
        userId: UserId,
        name: String,
        description: String?,
        isPublic: Boolean,
    ): KnowledgeBase = tx.execute {
        val kb = KnowledgeBase.create(
            name = name,
            description = description,
            authorId = userId,
            isPublic = isPublic,
        )
        knowledgeBaseRepository.save(kb)
        kb.publishAndClear(domainEventBus)
        kb
    }

    override suspend fun updateKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        name: String,
        description: String?,
        isPublic: Boolean,
    ): KnowledgeBase = tx.execute {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }
        kb.update(
            name = name,
            description = description,
            isPublic = isPublic,
        )
        knowledgeBaseRepository.save(kb)
        kb.publishAndClear(domainEventBus)
        kb
    }

    override suspend fun deleteKnowledgeBase(userId: UserId, knowledgeBaseId: KnowledgeBaseId) = tx.execute {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("知识库不存在")
        if (kb.authorId != userId) {
            throw AuthorizationException("无权访问")
        }
        kb.markDeleted()
        // 级联删除文档和文件
        documentRepository.deleteByKnowledgeBaseId(knowledgeBaseId)
        fileRepository.deleteByKnowledgeBaseId(knowledgeBaseId)
        knowledgeBaseRepository.deleteById(knowledgeBaseId)
        kb.publishAndClear(domainEventBus)
    }

    override suspend fun getKnowledgeBaseStats(userId: UserId?, knowledgeBaseId: KnowledgeBaseId): KnowledgeBaseStats =
        tx.readOnly {
            val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
                ?: throw NotFoundException("知识库不存在")
            if (!kb.isPublic && kb.authorId != userId) {
                throw AuthorizationException("无权访问")
            }
            val fileCount = fileRepository.findByKnowledgeBaseId(knowledgeBaseId).size
            val documentCount = documentRepository.countByKnowledgeBaseId(knowledgeBaseId)
            KnowledgeBaseStats(
                knowledgeBaseId = knowledgeBaseId,
                fileCount = fileCount,
                documentCount = documentCount,
            )
        }
}

