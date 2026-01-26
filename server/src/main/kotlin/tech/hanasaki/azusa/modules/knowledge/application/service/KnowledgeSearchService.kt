package tech.hanasaki.azusa.modules.knowledge.application.service

import tech.hanasaki.azusa.modules.knowledge.domain.port.EmbeddingService
import tech.hanasaki.azusa.modules.knowledge.domain.port.SearchResult
import tech.hanasaki.azusa.modules.knowledge.domain.port.VectorStore
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeBaseRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.UserId

class KnowledgeSearchService(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val embeddingService: EmbeddingService,
    private val vectorStore: VectorStore,
) {

    /**
     * 搜索知识库
     */
    suspend fun search(
        userId: UserId,
        knowledgeBaseIds: List<KnowledgeBaseId>,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult> {
        if (knowledgeBaseIds.isEmpty()) {
            return emptyList()
        }

        // 验证用户对所有知识库的访问权限
        for (kbId in knowledgeBaseIds) {
            val kb = knowledgeBaseRepository.findById(kbId)
                ?: throw NotFoundException("Knowledge base not found: ${kbId.value}")
            if (!kb.isPublic && kb.authorId != userId) {
                throw AuthorizationException("Access denied to knowledge base: ${kbId.value}")
            }
        }

        // 生成查询的 embedding
        val queryEmbedding = embeddingService.embed(query)

        // 执行向量搜索
        return vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = knowledgeBaseIds,
            threshold = threshold,
            limit = limit,
        )
    }

    /**
     * 搜索单个知识库
     */
    suspend fun searchKnowledgeBase(
        userId: UserId,
        knowledgeBaseId: KnowledgeBaseId,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult> {
        return search(
            userId = userId,
            knowledgeBaseIds = listOf(knowledgeBaseId),
            query = query,
            threshold = threshold,
            limit = limit,
        )
    }

    /**
     * 搜索用户的所有知识库
     */
    suspend fun searchMyKnowledgeBases(
        userId: UserId,
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult> {
        val myKnowledgeBases = knowledgeBaseRepository.findByAuthorId(userId)
        if (myKnowledgeBases.isEmpty()) {
            return emptyList()
        }

        val queryEmbedding = embeddingService.embed(query)

        return vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = myKnowledgeBases.map { it.id },
            threshold = threshold,
            limit = limit,
        )
    }

    /**
     * 搜索公开知识库
     */
    suspend fun searchPublicKnowledgeBases(
        query: String,
        threshold: Float = 0.7f,
        limit: Int = 10,
    ): List<SearchResult> {
        val publicKnowledgeBases = knowledgeBaseRepository.findPublic()
        if (publicKnowledgeBases.isEmpty()) {
            return emptyList()
        }

        val queryEmbedding = embeddingService.embed(query)

        return vectorStore.search(
            queryEmbedding = queryEmbedding,
            knowledgeBaseIds = publicKnowledgeBases.map { it.id },
            threshold = threshold,
            limit = limit,
        )
    }
}
