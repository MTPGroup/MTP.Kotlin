package tech.hanasaki.azusa.modules.knowledge

import com.zaxxer.hikari.HikariDataSource
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.knowledge.adapter.out.ingestor.Lc4jDocumentIngestorAdapter
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger {}

/**
 * 手动测试 LangChain4j EmbeddingStore
 * 运行前请确保：
 * 1. PostgreSQL 正在运行，且已安装 pgvector 扩展
 * 2. Ollama 正在运行，且有 bge-m3 模型
 * 3. 数据库 azusa 存在
 *
 * 运行方式：
 * ./gradlew :server:test --tests "tech.hanasaki.azusa.modules.knowledge.LangchainEmbeddingManualTest.testDirectStore" -i
 */
class LangchainEmbeddingManualTest {

    companion object {
        // 数据库配置
        private const val DB_URL = "jdbc:postgresql://localhost:5432/azusa"
        private const val DB_USER = "postgres"
        private const val DB_PASSWORD = "postgres"
        private const val OLLAMA_URL = "http://localhost:11434"

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info { "=== 手动测试 LangChain4j EmbeddingStore ===" }

            // 创建数据源
            val dataSource = HikariDataSource().apply {
                jdbcUrl = DB_URL
                username = DB_USER
                password = DB_PASSWORD
                maximumPoolSize = 5
            }

            try {
                // 创建 EmbeddingModel
                val embeddingModel = OllamaEmbeddingModel.builder()
                    .baseUrl(OLLAMA_URL)
                    .modelName("bge-m3")
                    .build()

                logger.info { "EmbeddingModel 维度: ${embeddingModel.dimension()}" }

                // 测试 1: 直接使用 EmbeddingStore API
                testDirectStore(dataSource, embeddingModel)

                // 测试 2: 使用 Ingestor
                testIngestor(dataSource, embeddingModel)

            } finally {
                dataSource.close()
            }
        }

        private fun testDirectStore(dataSource: HikariDataSource, embeddingModel: EmbeddingModel) {
            logger.info { "\n=== 测试 1: 直接使用 EmbeddingStore API ===" }

            // 创建 EmbeddingStore（自动建表）
            val embeddingStore: EmbeddingStore<TextSegment> = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("test_embeddings")
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .build()

            // 添加测试数据
            val segment = TextSegment.from(
                "测试文本内容",
                Metadata().apply {
                    put("test", "true")
                    put("knowledgeBaseId", "test-kb")
                }
            )

            val embedding = embeddingModel.embed(segment).content()
            logger.info { "生成 embedding，维度: ${embedding.vector().size}" }

            embeddingStore.add(embedding, segment)
            logger.info { "已添加到 store" }

            // 搜索验证
            val queryEmbedding = embeddingModel.embed("测试").content()
            val results = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(1)
                    .build()
            ).matches()

            logger.info { "搜索结果数量: ${results.size}" }
            results.firstOrNull()?.let {
                logger.info { "Top 1 分数: ${it.score()}" }
                logger.info { "Top 1 文本: ${it.embedded().text()}" }
            }
        }

        private fun testIngestor(dataSource: HikariDataSource, embeddingModel: EmbeddingModel) {
            logger.info { "\n=== 测试 2: 使用 Ingestor ===" }

            // 创建 Ingestor 使用的 EmbeddingStore
            val embeddingStore: EmbeddingStore<TextSegment> = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("test_ingestor_embeddings")
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .build()

            // 创建 Ingestor
            val ingestor = Lc4jDocumentIngestorAdapter(
                embeddingModel = embeddingModel,
                embeddingStore = embeddingStore,
                maxSegmentSize = 500,
                maxOverlapSize = 50
            )

            // 摄取文档
            val content = "这是一段长文本内容，用于测试 Ingestor 是否能正确分割、嵌入和存储到数据库中。"
            val knowledgeBaseId = KnowledgeBaseId(Uuid.random())
            val fileId = KnowledgeFileId(Uuid.random())

            logger.info { "开始摄取文档..." }
            try {
                runBlocking {
                    ingestor.ingest(
                        content = content,
                        knowledgeBaseId = knowledgeBaseId,
                        fileId = fileId,
                        filePath = "test/document.txt",
                        extraMetadata = JsonObject(emptyMap())
                    )
                }
                logger.info { "文档摄取完成" }
            } catch (e: Exception) {
                logger.error(e) { "文档摄取失败" }
            }

            // 搜索验证
            val queryEmbedding = embeddingModel.embed("测试文本").content()
            val results = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .build()
            ).matches()

            logger.info { "摄取后搜索结果: ${results.size}" }
            results.forEachIndexed { index, match ->
                logger.info { "  [$index] 分数: ${match.score()}, 文本: ${match.embedded().text().take(50)}..." }
            }
        }
    }
}
