package tech.hanasaki.azusa.shared.infrastructure.llm

import com.zaxxer.hikari.HikariDataSource
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.server.config.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.ChatModelFactory
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig

fun llmModule(config: ApplicationConfig) = module {
    val embeddingConfig = config.readEmbeddingConfig()

    single { ChatModelFactory(get<LLMConfig>(named("official"))) }
    single<HttpClient> {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 5_000
            }
        }
    }

    single<EmbeddingModel> {
        if (embeddingConfig.provider == "ollama") {
            OllamaEmbeddingModel.builder()
                .baseUrl(embeddingConfig.baseUrl)
                .modelName(embeddingConfig.model)
                .build()
        } else {
            OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingConfig.baseUrl)
                .modelName(embeddingConfig.model)
                .apiKey(embeddingConfig.apiKey)
                .build()
        }
    }
    single<EmbeddingStore<TextSegment>> {
        val datasource = get<HikariDataSource>()

        PgVectorEmbeddingStore.datasourceBuilder()
            .datasource(datasource)
            .table("langchain_embeddings")
            .dimension(embeddingConfig.dimension)
            .createTable(false)
            .build()
    }
}
