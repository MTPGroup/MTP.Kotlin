package tech.hanasaki.azusa.modules.knowledge.application.service

import tech.hanasaki.azusa.modules.knowledge.application.command.CreateKnowledgeBaseCommand
import tech.hanasaki.azusa.modules.knowledge.application.command.UpdateKnowledgeBaseCommand
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeFileRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.OutboxSchedulerPort

class KnowledgeBaseService(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val fileRepository: KnowledgeFileRepository,
    private val documentRepository: KnowledgeDocumentRepository,
    private val outboxScheduler: OutboxSchedulerPort,
) {

    /**
     * 获取公开的知识库列表
     */
    suspend fun listPublicKnowledgeBases(page: Int, limit: Int): PageResult<KnowledgeBase> {
        return knowledgeBaseRepository.findPublicPaged(page, limit)
    }

    /**
     * 搜索公开的知识库
     */
    suspend fun searchKnowledgeBases(query: String, page: Int, limit: Int): PageResult<KnowledgeBase> {
        return knowledgeBaseRepository.searchPublic(query, page, limit)
    }

    /**
     * 获取用户的知识库列表
     */
    suspend fun listMyKnowledgeBases(userId: UserId, page: Int, limit: Int): PageResult<KnowledgeBase> {
        return knowledgeBaseRepository.findByAuthorIdPaged(userId, page, limit)
    }

    /**
     * 获取知识库详情
     */
    suspend fun getKnowledgeBase(userId: UserId, knowledgeBaseId: KnowledgeBaseId): KnowledgeBase {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (!kb.isPublic && kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        return kb
    }

    /**
     * 创建知识库
     */
    suspend fun createKnowledgeBase(userId: UserId, cmd: CreateKnowledgeBaseCommand): KnowledgeBase {
        val kb = KnowledgeBase.create(
            name = cmd.name,
            description = cmd.description,
            authorId = userId,
            isPublic = cmd.isPublic,
        )
        knowledgeBaseRepository.save(kb)
        kb.publishAndClear(outboxScheduler)
        return kb
    }

    /**
     * 更新知识库
     */
    suspend fun updateKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        cmd: UpdateKnowledgeBaseCommand,
    ): KnowledgeBase {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        kb.update(
            name = cmd.name,
            description = cmd.description,
            isPublic = cmd.isPublic,
        )
        knowledgeBaseRepository.save(kb)
        kb.publishAndClear(outboxScheduler)
        return kb
    }

    /**
     * 删除知识库
     */
    suspend fun deleteKnowledgeBase(userId: UserId, knowledgeBaseId: KnowledgeBaseId) {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        kb.markDeleted()
        // 级联删除文档和文件
        documentRepository.deleteByKnowledgeBaseId(knowledgeBaseId)
        fileRepository.deleteByKnowledgeBaseId(knowledgeBaseId)
        knowledgeBaseRepository.deleteById(knowledgeBaseId)
        kb.publishAndClear(outboxScheduler)
    }

    /**
     * 获取知识库统计信息
     */
    suspend fun getKnowledgeBaseStats(userId: UserId, knowledgeBaseId: KnowledgeBaseId): KnowledgeBaseStats {
        val kb = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw NotFoundException("Knowledge base not found")
        if (kb.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        val fileCount = fileRepository.findByKnowledgeBaseId(knowledgeBaseId).size
        val documentCount = documentRepository.countByKnowledgeBaseId(knowledgeBaseId)
        return KnowledgeBaseStats(
            knowledgeBaseId = knowledgeBaseId,
            fileCount = fileCount,
            documentCount = documentCount,
        )
    }

}

data class KnowledgeBaseStats(
    val knowledgeBaseId: KnowledgeBaseId,
    val fileCount: Int,
    val documentCount: Long,
)
