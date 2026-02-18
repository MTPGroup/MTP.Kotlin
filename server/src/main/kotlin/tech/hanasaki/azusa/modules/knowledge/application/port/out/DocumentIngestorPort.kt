package tech.hanasaki.azusa.modules.knowledge.application.port.out

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId

/**
 * 文档摄取端口
 * 抽象文档分割、Embedding、存储的完整流程
 */
interface DocumentIngestorPort {
    /**
     * 摄取单个文档
     */
    suspend fun ingest(
        content: String,
        knowledgeBaseId: KnowledgeBaseId,
        fileId: KnowledgeFileId,
        filePath: String,
        extraMetadata: JsonObject = JsonObject(emptyMap()),
    )

    /**
     * 批量摄取
     */
    suspend fun ingestBatch(requests: List<IngestRequest>)

    data class IngestRequest(
        val content: String,
        val knowledgeBaseId: KnowledgeBaseId,
        val fileId: KnowledgeFileId,
        val filePath: String,
        val extraMetadata: JsonObject = JsonObject(emptyMap()),
    )
}
