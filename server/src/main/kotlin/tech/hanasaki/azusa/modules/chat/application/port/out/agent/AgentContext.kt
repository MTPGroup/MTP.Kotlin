package tech.hanasaki.azusa.modules.chat.application.port.out.agent

import ai.koog.agents.core.tools.ToolRegistry
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig

data class AgentContext(
    val chat: Chat,
    val originPrompt: String,
    val chatConfig: ChatConfig?,
    val knowledgeBaseIds: List<String>,
    val toolRegistry: ToolRegistry,
    val recentHistory: List<Message>,
    val effectiveLLMConfig: LLMConfig,
)