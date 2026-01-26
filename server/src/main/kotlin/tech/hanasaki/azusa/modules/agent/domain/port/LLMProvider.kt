package tech.hanasaki.azusa.modules.agent.domain.port

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.common.kernel.model.LLMConfig

/**
 * LLM 提供者端口接口
 * 用于调用各种 LLM 服务（OpenAI, Anthropic 等）
 */
interface LLMProvider {
    /**
     * 执行 LLM 调用（非流式）
     */
    suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>?,
        config: LLMConfig,
    ): LLMResponse

    /**
     * 流式 LLM 调用
     */
    fun chatStream(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>?,
        config: LLMConfig,
    ): Flow<LLMStreamEvent>
}

/**
 * 聊天消息
 */
@Serializable
data class ChatMessage(
    val role: String,  // "user", "assistant", "system"
    val content: String,
    val toolCalls: List<ToolCall>? = null,
)

/**
 * 工具定义
 */
@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

/**
 * LLM 响应
 */
@Serializable
data class LLMResponse(
    val content: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val stopReason: String,  // "stop", "end_turn", "tool_calls"
)

/**
 * 工具调用
 */
@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: JsonObject,
)

/**
 * LLM 流式事件
 */
sealed class LLMStreamEvent {
    data class Delta(val text: String) : LLMStreamEvent()
    data class ToolCallStart(val toolCall: ToolCall) : LLMStreamEvent()
    data class Done(val content: String) : LLMStreamEvent()
}
