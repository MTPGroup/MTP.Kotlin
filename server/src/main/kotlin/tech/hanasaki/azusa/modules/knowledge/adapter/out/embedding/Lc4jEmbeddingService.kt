package tech.hanasaki.azusa.modules.knowledge.adapter.out.embedding

import dev.langchain4j.model.embedding.EmbeddingModel
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort

/**
 * 基于 LangChain4j 的 Embedding 服务实现
 * 使用 OllamaEmbeddingModel 替代 Koog 的 LLMEmbedder
 */
class Lc4jEmbeddingService(
    private val embeddingModel: EmbeddingModel,
) : EmbeddingServicePort {

    override suspend fun embed(text: String): FloatArray {
        val embedding = embeddingModel.embed(text)
        return embedding.content().vector()
    }

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> {
        return texts.map { embed(it) }
    }
}