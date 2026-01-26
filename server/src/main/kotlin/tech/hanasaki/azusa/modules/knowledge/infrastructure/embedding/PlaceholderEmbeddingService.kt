package tech.hanasaki.azusa.modules.knowledge.infrastructure.embedding

import tech.hanasaki.azusa.modules.knowledge.domain.port.EmbeddingService

/**
 * 占位 Embedding 服务
 * TODO: 替换为实际的 LLM Embedding 服务实现（如 OpenAI、Cohere、Ollama等）
 */
class PlaceholderEmbeddingService : EmbeddingService {

    companion object {
        private const val EMBEDDING_DIMENSION = 1024
    }

    override suspend fun embed(text: String): FloatArray {
        // 生成随机向量作为占位符
        // 实际实现应调用 LLM API 生成真实的向量
        return FloatArray(EMBEDDING_DIMENSION) { Math.random().toFloat() }
    }

    override suspend fun embedBatch(texts: List<String>): List<FloatArray> {
        return texts.map { embed(it) }
    }
}
