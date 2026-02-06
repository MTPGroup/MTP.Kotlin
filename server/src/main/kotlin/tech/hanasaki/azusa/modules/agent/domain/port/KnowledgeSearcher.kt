package tech.hanasaki.azusa.modules.agent.domain.port

import tech.hanasaki.azusa.shared.domain.model.vo.UserId

/**
 * 知识库搜索器端口接口
 * 用于检索 RAG（Retrieval-Augmented Generation）知识库
 */
interface KnowledgeSearcher {
    /**
     * 搜索知识库
     *
     * @param userId 用户 ID
     * @param knowledgeBaseIds 知识库 ID 列表
     * @param query 搜索查询
     * @param limit 返回结果数量
     * @return 搜索结果列表
     */
    suspend fun search(
        userId: UserId,
        knowledgeBaseIds: List<String>,
        query: String,
        limit: Int = 5,
    ): List<SearchResult>
}

/**
 * 搜索结果
 */
data class SearchResult(
    val content: String,
    val similarity: Float,
    val source: String? = null,
)
