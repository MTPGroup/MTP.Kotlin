package tech.hanasaki.azusa.modules.knowledge.adapter.out.ingestor

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentIngestorPort
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId


/**
 * 基于 LangChain4j EmbeddingStoreIngestor 的文档摄取服务
 * 替代手动 chunk -> embed -> save 流程
 */
class Lc4jDocumentIngestorAdapter(
    private val embeddingModel: EmbeddingModel,
    private val embeddingStore: EmbeddingStore<TextSegment>,
    private val maxSegmentSize: Int = 500,
    private val maxOverlapSize: Int = 50,
) : DocumentIngestorPort {
    private val logger = KotlinLogging.logger { }

    private val ingestor: EmbeddingStoreIngestor by lazy {
        EmbeddingStoreIngestor.builder()
            .documentSplitter(
                DocumentSplitters.recursive(maxSegmentSize, maxOverlapSize)
            )
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()
    }

    /**
     * 摄取文档内容
     * @param content 原始文档内容
     * @param knowledgeBaseId 知识库ID
     * @param fileId 文件ID
     * @param filePath 文件路径
     * @param extraMetadata 额外元数据
     */
    override suspend fun ingest(
        content: String,
        knowledgeBaseId: KnowledgeBaseId,
        fileId: KnowledgeFileId,
        filePath: String,
        extraMetadata: JsonObject,
    ) {
        logger.info { "[LangChain4j] 开始摄取文档: $filePath" }

        try {
            val metadata = buildMetadata(
                knowledgeBaseId = knowledgeBaseId,
                fileId = fileId,
                filePath = filePath,
                extraMetadata = extraMetadata,
            )
            val document = Document.from(content, metadata)

            ingestor.ingest(document)

            logger.info { "[LangChain4j] 文档摄取完成: $filePath" }
        } catch (e: Exception) {
            logger.error(e) { "[LangChain4j] 文档摄取失败: $filePath" }
            throw e
        }
    }

    /**
     * 批量摄取多个文档
     */
    override suspend fun ingestBatch(requests: List<DocumentIngestorPort.IngestRequest>) {
        if (requests.isEmpty()) return

        logger.debug { "批量摄取 ${requests.size} 个文档" }

        val lcDocuments = requests.map { req ->
            val metadata = buildMetadata(
                knowledgeBaseId = req.knowledgeBaseId,
                fileId = req.fileId,
                filePath = req.filePath,
                extraMetadata = req.extraMetadata,
            )
            Document.from(req.content, metadata)
        }

        ingestor.ingest(*lcDocuments.toTypedArray())

        logger.debug { "批量摄取完成" }
    }

    private fun buildMetadata(
        knowledgeBaseId: KnowledgeBaseId,
        fileId: KnowledgeFileId,
        filePath: String,
        extraMetadata: JsonObject = JsonObject(emptyMap()),
    ) = Metadata().apply {
        put("knowledgeBaseId", knowledgeBaseId.value.toString())
        put("fileId", fileId.value.toString())
        put("filePath", filePath)
        extraMetadata.forEach { (key, value) ->
            put(key, value.toString())
        }
    }
}
