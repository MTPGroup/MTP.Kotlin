package tech.hanasaki.azusa.modules.chat.adapter.out.knowledge

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class SearchKnowledgeTool(
    private val knowledgeSearcher: KnowledgeSearcherPort,
    private val userId: UserId,
    private val knowledgeBaseIds: List<String>,
) {
    private val logger = KotlinLogging.logger { }

    @Tool("从知识库中搜索相关信息。当需要查找事实性信息、背景知识时使用。")
    fun search_knowledge(@P("搜索查询关键词") query: String): String = runBlocking {
        logger.debug { "---------------------开始检索---------------------" }
        val results = knowledgeSearcher.search(userId, knowledgeBaseIds, query)
        logger.debug { "---------------------结束检索---------------------" }
        results.joinToString("\n\n") { "【${it.source ?: "知识库"}】${it.content}" }
    }
}
