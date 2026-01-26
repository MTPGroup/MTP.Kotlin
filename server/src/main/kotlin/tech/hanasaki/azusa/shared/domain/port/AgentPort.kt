package tech.hanasaki.azusa.shared.domain.port

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId

/**
 * Agent 服务端口（被 Chat、Plugin 等模块依赖）
 * 具体实现在 Agent 模块中
 */
interface AgentPort {
    /**
     * 执行 Agent 并获取完整响应
     */
    suspend fun execute(context: AgentContext): AgentResponse

    /**
     * 流式执行 Agent，实时推送响应
     */
    fun executeStream(context: AgentContext): Flow<AgentStreamEvent>
}

/**
 * Agent 执行上下文
 */
data class AgentContext(
    val userId: UserId,
    val chatId: ChatId,
    val characterId: CharacterId,
    val character: Any,  // Character 对象（避免循环依赖）
    val userMessage: String,
    val chatHistory: List<Any>,  // Message 列表
    val llmConfig: Any,  // LLMConfig 对象
    val subscribedPlugins: List<Any> = emptyList(),
    val subscribedKnowledgeBases: List<Any> = emptyList(),
)

/**
 * Agent 响应
 */
sealed class AgentResponse {
    data class Success(
        val content: String,
        val toolCalls: List<ToolCallResult> = emptyList(),
    ) : AgentResponse()

    data class Error(val message: String) : AgentResponse()
}

/**
 * 流式事件
 */
sealed class AgentStreamEvent {
    data class ToolCall(val toolName: String, val arguments: JsonObject) : AgentStreamEvent()
    data class ToolResult(val toolName: String, val result: String) : AgentStreamEvent()
    data class ContentDelta(val text: String) : AgentStreamEvent()
    data class Done(val content: String) : AgentStreamEvent()
}

/**
 * 工具调用结果
 */
data class ToolCallResult(
    val toolName: String,
    val arguments: JsonObject,
    val result: String,
)
