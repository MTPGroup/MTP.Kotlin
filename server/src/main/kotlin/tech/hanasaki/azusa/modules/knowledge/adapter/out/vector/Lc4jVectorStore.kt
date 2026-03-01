package tech.hanasaki.azusa.modules.knowledge.adapter.out.vector

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.filter.Filter
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo
import dev.langchain4j.store.embedding.filter.comparison.IsIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import tech.hanasaki.azusa.modules.knowledge.application.port.out.SearchResult
import tech.hanasaki.azusa.modules.knowledge.application.port.out.VectorStore
import tech.hanasaki.azusa.shared.domain.exception.InternalServerException
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeDocumentId
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger {}

/**
 * 基于 LangChain4j EmbeddingStore 的向量存储实现
 * 替代 PgVectorStore 使用原生 SQL 查询的方式
 */
class Lc4jVectorStore(
    private val embeddingStore: EmbeddingStore<TextSegment>,
) : VectorStore {

    override suspend fun search(
        queryEmbedding: FloatArray,
        knowledgeBaseIds: List<KnowledgeBaseId>,
        threshold: Float,
        limit: Int,
    ): List<SearchResult> {
        if (knowledgeBaseIds.isEmpty()) {
            logger.warn { "知识库ID为空" }
            return emptyList()
        }

        logger.debug { "使用 Lc4jVectorStore 搜索知识库: ${knowledgeBaseIds.map { it.value }}" }

        // 构建 metadata 过滤器：knowledgeBaseId 在给定列表中
        val filter = buildKnowledgeBaseFilter(knowledgeBaseIds)

        // 构建搜索请求
        val searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(Embedding.from(queryEmbedding.toList()))
            .filter(filter)
            .maxResults(limit)
            .minScore(threshold.toDouble())
            .build()

        // 执行搜索
        val matches = embeddingStore.search(searchRequest).matches()

        logger.debug { "搜索完成，找到 ${matches.size} 条结果" }

        return matches.map { it.toSearchResult() }
    }

    /**
     * 构建知识库过滤器
     */
    private fun buildKnowledgeBaseFilter(knowledgeBaseIds: List<KnowledgeBaseId>): Filter {
        val kbIdStrings = knowledgeBaseIds.map { it.value.toString() }

        return when (kbIdStrings.size) {
            1 -> IsEqualTo("knowledgeBaseId", kbIdStrings.first())
            else -> IsIn("knowledgeBaseId", kbIdStrings)
        }
    }

    /**
     * 将 LangChain4j 的 EmbeddingMatch 转换为领域层的 SearchResult
     */
    private fun EmbeddingMatch<TextSegment>.toSearchResult(): SearchResult {
        val segment = this.embedded()
        val metadata = segment.metadata()

        // 提取 metadata
        val jsonMetadata = try {
            val metadataMap = metadata.toMap()
            JsonObject(metadataMap.mapValues {
                JsonPrimitive(it.value.toString())
            })
        } catch (e: Exception) {
            logger.warn(e) { "解析 metadata 失败" }
            JsonObject(emptyMap())
        }

        val fileId = metadata.getString("fileId")
            ?: throw InternalServerException("向量检索结果缺少 fileId 元数据")
        val knowledgeBaseId = metadata.getString("knowledgeBaseId")
            ?: throw InternalServerException("向量检索结果缺少 knowledgeBaseId 元数据")

        return SearchResult(
            documentId = KnowledgeDocumentId(Uuid.parse(fileId)),
            knowledgeBaseId = KnowledgeBaseId(Uuid.parse(knowledgeBaseId)),
            content = segment.text(),
            metadata = jsonMetadata,
            similarity = this.score().toFloat(),
        )
    }
}
