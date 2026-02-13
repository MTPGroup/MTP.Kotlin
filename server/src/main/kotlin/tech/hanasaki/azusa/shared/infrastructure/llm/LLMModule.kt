package tech.hanasaki.azusa.shared.infrastructure.llm

import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMProvider
import com.zaxxer.hikari.HikariDataSource
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import org.koin.dsl.module

fun llmModule() = module {
    single<OllamaClient> { OllamaClient() }
    single<DeepSeekLLMClient> { DeepSeekLLMClient("sk-ef5a723504f147efaadaa78c0828ee73") }
    single<PromptExecutor> {
        MultiLLMPromptExecutor(
            LLMProvider.Ollama to get<OllamaClient>(),
            LLMProvider.DeepSeek to get<DeepSeekLLMClient>(),
        )
    }
    single<HttpClient> {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 5_000
            }
        }
    }

    single<EmbeddingModel> {
        OllamaEmbeddingModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("bge-m3")
            .build()
    }
    single<EmbeddingStore<TextSegment>> {
        val datasource = get<HikariDataSource>()
        val embeddingModel = get<EmbeddingModel>()
        val logger = KotlinLogging.logger { }
        logger.error { "向量维度: ${embeddingModel.dimension()}" }

        PgVectorEmbeddingStore.datasourceBuilder()
            .datasource(datasource)
            .table("langchain_embeddings")
            .dimension(embeddingModel.dimension())
            .createTable(false)
            .build()
    }
}
