package tech.hanasaki.azusa.shared.infrastructure.llm

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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.ChatModelFactory
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig

fun llmModule() = module {
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
        OllamaEmbeddingModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("bge-m3")
            .build()
    }
    single<EmbeddingStore<TextSegment>> {
        val datasource = get<HikariDataSource>()
        val embeddingModel = get<EmbeddingModel>()
        val logger = KotlinLogging.logger { }

        PgVectorEmbeddingStore.datasourceBuilder()
            .datasource(datasource)
            .table("langchain_embeddings")
            .dimension(1024)
            .createTable(false)
            .build()
    }
}
