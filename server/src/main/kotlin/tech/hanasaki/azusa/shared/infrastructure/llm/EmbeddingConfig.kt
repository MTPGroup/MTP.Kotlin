package tech.hanasaki.azusa.shared.infrastructure.llm

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.optionalInt
import tech.hanasaki.azusa.shared.infrastructure.config.optionalString
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class EmbeddingConfig(
    val provider: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
    val dimension: Int,
)

fun ApplicationConfig.readEmbeddingConfig(): EmbeddingConfig =
    EmbeddingConfig(
        provider = optionalString("embedding.provider") ?: "ollama",
        baseUrl = requireString("embedding.baseUrl"),
        model = requireString("embedding.model"),
        apiKey = requireString("embedding.apiKey"),
        dimension = optionalInt("embedding.dimension") ?: 1024,
    )
