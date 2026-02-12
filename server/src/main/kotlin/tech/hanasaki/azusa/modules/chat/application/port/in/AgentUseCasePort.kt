package tech.hanasaki.azusa.modules.chat.application.port.`in`

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface AgentUseCasePort {
    fun processMessage(
        userId: UserId,
        chatId: ChatId,
        userMessage: List<MessageContent>,
    ): Flow<AgentStreamEvent>
}

sealed class AgentStreamEvent {
    data class Delta(val text: String) : AgentStreamEvent()
    data class ToolCallStart(val name: String, val arguments: JsonObject) : AgentStreamEvent()
    data class ToolCallResult(val name: String, val result: String) : AgentStreamEvent()
    data class Done(val fullContent: String) : AgentStreamEvent()
    data class Error(val message: String) : AgentStreamEvent()
}
