package tech.hanasaki.azusa.modules.knowledge.domain.port

/**
 * 向量化服务端口
 */
interface EmbeddingService {
    /**
     * 对单个文本进行向量化
     */
    suspend fun embed(text: String): FloatArray

    /**
     * 批量向量化
     */
    suspend fun embedBatch(texts: List<String>): List<FloatArray>
}
