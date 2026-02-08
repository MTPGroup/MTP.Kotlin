package tech.hanasaki.azusa.modules.knowledge.adapter.out.embedding

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort

class KoogEmbeddingService(
    client: OllamaClient,
) : EmbeddingServicePort {
    private val embedder = LLMEmbedder(client, QWEN3_EMBEDDING)


    companion object {
        private val QWEN3_EMBEDDING = LLModel(
            provider = LLMProvider.Ollama,
            id = "bge-m3",
            capabilities = listOf(LLMCapability.Embed),
            contextLength = 8_192
        )
    }

    override suspend fun embed(text: String): FloatArray {
        val vector = embedder.embed(text)
        return vector.values.map { it.toFloat() }.toFloatArray()
    }

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> {
        return texts.map { embed(it) }
    }
}
