package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 消息角色枚举
 */
enum class MessageRole {
    USER,       // 用户消息
    ASSISTANT,  // AI 助手消息
    SYSTEM,     // 系统消息
}

/**
 * Message 实体 - 表示聊天中的一条消息
 */
data class Message(
    val id: MessageId,
    val chatId: ChatId,
    val role: MessageRole,
    val content: String,              // 纯文本内容
    val metadata: JsonObject?,        // 额外信息（如工具调用）
    val createdAt: Instant,
) {
    companion object {
        /**
         * 创建新消息
         */
        fun create(
            chatId: ChatId,
            role: MessageRole,
            content: String,
            metadata: JsonObject? = null,
        ): Message {
            return Message(
                id = MessageId(java.util.UUID.randomUUID()),
                chatId = chatId,
                role = role,
                content = content,
                metadata = metadata,
                createdAt = Clock.System.now(),
            )
        }

        /**
         * 从持久化层重建消息
         */
        fun reconstitute(
            id: MessageId,
            chatId: ChatId,
            role: MessageRole,
            content: String,
            metadata: JsonObject?,
            createdAt: Instant,
        ): Message = Message(
            id = id,
            chatId = chatId,
            role = role,
            content = content,
            metadata = metadata,
            createdAt = createdAt,
        )
    }
}
