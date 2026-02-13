package tech.hanasaki.azusa.modules.chat.adapter.out.knowledge

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.chat.application.port.out.knowledge.KnowledgeSearcher
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class SearchKnowledgeTool(
    private val knowledgeSearcher: KnowledgeSearcher,
    private val userId: UserId,
    private val knowledgeBaseIds: List<String>,
) : SimpleTool<SearchKnowledgeTool.Args>(
    argsSerializer = Args.serializer(),
    name = "search_knowledge",
    description = "从知识库中搜索相关信息。当需要查找事实性信息、背景知识时使用。",
) {
    private val logger = KotlinLogging.logger { }

    @Serializable
    data class Args(
        @property:LLMDescription("搜索查询关键词")
        val query: String,
    )

    override suspend fun execute(args: Args): String {
        val results = knowledgeSearcher.search(userId, knowledgeBaseIds, args.query)
        logger.debug { "检索知识库结果为：$results" }
        return results.joinToString("\n\n") { "【${it.source ?: "知识库"}】${it.content}" }
    }
}
