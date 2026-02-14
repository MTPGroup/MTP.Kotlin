package tech.hanasaki.azusa.modules.chat.application.port.out

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.service.tool.ToolExecutor
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

data class AgentContext(
    val chat: Chat,
    val originPrompt: String,
    val chatConfig: ChatConfig?,
    val knowledgeBaseIds: List<String>,
    val pluginTools: Map<ToolSpecification, ToolExecutor>,
    val toolObjects: List<Any>,
    val recentHistory: List<Message>,
    val effectiveLLMConfig: LLMConfig,
)

interface AgentContextLoaderPort {
    suspend fun load(userId: UserId, chatId: ChatId, requestId: String): AgentContext
}

