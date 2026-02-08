package tech.hanasaki.azusa.modules.knowledge.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeSearchUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.SearchResult
import tech.hanasaki.azusa.modules.knowledge.application.port.out.VectorStore
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class KnowledgeSearchService(
    private val knowledgeBaseRepository: KnowledgeBaseRepositoryPort,
    private val embeddingService: EmbeddingServicePort,
    private val vectorStore: VectorStore,
    private val tx: TransactionalPort,
) : KnowledgeSearchUseCasePort {
    private val logger = KotlinLogging.logger { }

    override suspend fun search(
        userId: UserId,
        knowledgeBaseIds: List<KnowledgeBaseId>,
        query: String,
        threshold: Float,
        limit: Int,
    ): List<SearchResult> = tx.readOnly {
        if (knowledgeBaseIds.isEmpty()) {
            logger.warn { "知识库ID为空" }
            return@readOnly emptyList()
        }

        // 验证用户对所有知识库的访问权限
        for (kbId in knowledgeBaseIds) {
            val kb = knowledgeBaseRepository.findById(kbId)
                ?: throw NotFoundException("知识库不存在: ${kbId.value}")
            if (!kb.isPublic && kb.authorId != userId) {
                throw AuthorizationException("无权访问知识库: ${kbId.value}")
            }
        }

        // 生成查询的 embedding
        val queryEmbedding = embeddingService.embed(query)

        // 执行向量搜索
        vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = knowledgeBaseIds,
            threshold = threshold,
            limit = limit,
        )
    }

    override suspend fun searchKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        query: String,
        threshold: Float,
        limit: Int,
    ): List<SearchResult> = search(
        userId = userId,
        knowledgeBaseIds = listOf(knowledgeBaseId),
        query = query,
        threshold = threshold,
        limit = limit,
    )

    override suspend fun searchMyKnowledgeBases(
        userId: UserId,
        query: String,
        threshold: Float,
        limit: Int,
    ): List<SearchResult> = tx.readOnly {
        val myKnowledgeBases = knowledgeBaseRepository.findByAuthorId(userId)
        if (myKnowledgeBases.isEmpty()) {
            return@readOnly emptyList()
        }

        val queryEmbedding = embeddingService.embed(query)

        vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = myKnowledgeBases.map { it.id },
            threshold = threshold,
            limit = limit,
        )
    }

    override suspend fun searchPublicKnowledgeBases(
        query: String,
        threshold: Float,
        limit: Int,
    ): List<SearchResult> = tx.readOnly {
        val publicKnowledgeBases = knowledgeBaseRepository.findPublic()
        if (publicKnowledgeBases.isEmpty()) {
            return@readOnly emptyList()
        }

        val queryEmbedding = embeddingService.embed(query)

        vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = publicKnowledgeBases.map { it.id },
            threshold = threshold,
            limit = limit,
        )
    }
}
