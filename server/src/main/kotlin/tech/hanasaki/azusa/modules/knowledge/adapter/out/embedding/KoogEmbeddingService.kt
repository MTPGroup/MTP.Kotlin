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
        private const val EMBEDDING_DIMENSION = 1024
        private val QWEN3_EMBEDDING = LLModel(
            provider = LLMProvider.Ollama,
            id = "qwen3-embedding",
            capabilities = listOf(LLMCapability.Embed),
            contextLength = 40_960
        )
    }

    override suspend fun embed(text: String): FloatArray {
        val vector = embedder.embed(text)
        // TODO: 目前这样截取的方式不可取
        return vector.values.map { it.toFloat() }.take(EMBEDDING_DIMENSION).toFloatArray()
    }

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> {
        return texts.map { embed(it) }
    }
}
