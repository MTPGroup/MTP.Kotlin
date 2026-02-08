package tech.hanasaki.azusa.modules.knowledge.adapter.out.vector

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import tech.hanasaki.azusa.modules.knowledge.application.port.out.SearchResult
import tech.hanasaki.azusa.modules.knowledge.application.port.out.VectorStore
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeDocumentId
import java.sql.Connection
import kotlin.uuid.Uuid

/**
 * PostgreSQL pgvector 实现的向量存储
 */

class PgVectorStore : VectorStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val logger = KotlinLogging.logger { }

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

        logger.debug { "开始搜索知识库" }

        val embeddingStr = "[${queryEmbedding.joinToString(",")}]"
        val kbIdsArray = knowledgeBaseIds.map { it.value.toString() }.toTypedArray()

        val results = mutableListOf<SearchResult>()

        val sql = """
            SELECT id, knowledge_base_id, content, metadata, similarity
            FROM public.match_knowledge_documents(
                ?::extensions.vector(1024),
                ?::uuid[],
                ?,
                ?
            )
        """.trimIndent()

        val conn = TransactionManager.current().connection.connection as Connection
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, embeddingStr)
            stmt.setArray(2, conn.createArrayOf("uuid", kbIdsArray))
            stmt.setFloat(3, threshold)
            stmt.setInt(4, limit)

            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    results.add(
                        SearchResult(
                            documentId = KnowledgeDocumentId(Uuid.parse(rs.getString("id"))),
                            knowledgeBaseId = KnowledgeBaseId(Uuid.parse(rs.getString("knowledge_base_id"))),
                            content = rs.getString("content"),
                            metadata = json.decodeFromString(JsonObject.serializer(), rs.getString("metadata")),
                            similarity = rs.getFloat("similarity"),
                        )
                    )
                }
            }
        }

        logger.debug { "Query embedding: $embeddingStr" }
        logger.debug { "KB IDs: ${kbIdsArray.contentToString()}" }
        logger.debug { "Threshold: $threshold, Limit: $limit" }
        logger.debug { "Result count: ${results.size}" }
        logger.debug { "搜索结果为: $results" }

        return results
    }
}
