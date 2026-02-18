package tech.hanasaki.azusa.modules.knowledge

import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tech.hanasaki.azusa.shared.infrastructure.config.DatabaseConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

/**
 * 测试 LangChain4j EmbeddingStore 是否正常工作
 */
class LangchainEmbeddingTest : KoinComponent {

    private val embeddingModel: EmbeddingModel by inject()
    private val embeddingStore: EmbeddingStore<TextSegment> by inject()
    private val dbConfig: DatabaseConfig by inject()

    @Test
    fun `test embedding store add and search`() = runBlocking {
        logger.info { "=== 测试 EmbeddingStore ===" }
        logger.info { "数据库: ${dbConfig.url}" }

        // 1. 创建测试文本段
        val segment1 = TextSegment.from(
            "我喜欢足球。",
            Metadata().apply {
                put("knowledgeBaseId", "test-kb-1")
                put("fileId", "test-file-1")
            }
        )
        val segment2 = TextSegment.from(
            "今天天气很好。",
            Metadata().apply {
                put("knowledgeBaseId", "test-kb-1")
                put("fileId", "test-file-2")
            }
        )

        // 2. 生成 embedding
        logger.info { "正在生成 embedding..." }
        val embedding1 = embeddingModel.embed(segment1).content()
        val embedding2 = embeddingModel.embed(segment2).content()
        logger.info { "Embedding 1 维度: ${embedding1.vector().size}" }
        logger.info { "Embedding 2 维度: ${embedding2.vector().size}" }

        // 3. 添加到 store
        logger.info { "正在添加到 EmbeddingStore..." }
        embeddingStore.add(embedding1, segment1)
        embeddingStore.add(embedding2, segment2)
        logger.info { "添加完成" }

        // 4. 搜索测试
        val queryText = "你最喜欢的运动是什么？"
        logger.info { "搜索查询: $queryText" }
        val queryEmbedding = embeddingModel.embed(queryText).content()

        val searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(1)
            .build()

        val results = embeddingStore.search(searchRequest).matches()
        logger.info { "搜索结果数量: ${results.size}" }

        // 5. 验证结果
        assertTrue(results.isNotEmpty(), "搜索结果不应为空")
        val topResult = results.first()
        logger.info { "Top 1 分数: ${topResult.score()}" }
        logger.info { "Top 1 文本: ${topResult.embedded().text()}" }

        assertEquals("我喜欢足球。", topResult.embedded().text())
        assertTrue(topResult.score() > 0.5, "相似度应大于 0.5")

        logger.info { "=== 测试通过 ===" }
    }

    @Test
    fun `test ingestor workflow`() = runBlocking {
        logger.info { "=== 测试 Ingestor 工作流 ===" }

        // 使用 DocumentIngestorPort 测试完整流程
        val ingestor: tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentIngestorPort by inject()

        val content = "这是一段测试文本，用于验证 Ingestor 是否正常工作。"
        val knowledgeBaseId = tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId(
            kotlin.uuid.Uuid.parse("20819d64-a93d-415c-afe8-5d21d252d062")
        )
        val fileId = tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId(
            kotlin.uuid.Uuid.parse("51c25229-653f-4829-a3ce-4e01f6d86bcf")
        )

        logger.info { "开始摄取文档..." }
        ingestor.ingest(
            content = content,
            knowledgeBaseId = knowledgeBaseId,
            fileId = fileId,
            filePath = "test/path/document.txt"
        )
        logger.info { "文档摄取完成" }

        // 验证搜索
        val queryEmbedding = embeddingModel.embed("测试文本").content()
        val searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(1)
            .build()

        val results = embeddingStore.search(searchRequest).matches()
        logger.info { "摄取后搜索结果: ${results.size}" }

        assertTrue(results.isNotEmpty(), "摄取后应能搜索到结果")
        logger.info { "=== Ingestor 测试通过 ===" }
    }
}
