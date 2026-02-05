package tech.hanasaki.azusa.modules.knowledge.domain.port

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeDocumentId

/**
 * 向量存储端口
 */
interface VectorStore {
    /**
     * 相似度搜索
     */
    suspend fun search(
        queryEmbedding: FloatArray,
        knowledgeBaseIds: List<KnowledgeBaseId>,
        threshold: Float = 0.5f,
        limit: Int = 10,
    ): List<SearchResult>
}

/**
 * 搜索结果
 */
data class SearchResult(
    val documentId: KnowledgeDocumentId,
    val knowledgeBaseId: KnowledgeBaseId,
    val content: String,
    val metadata: JsonObject,
    val similarity: Float,
)
