package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Message 实体 - 表示聊天中的一条消息
 */
data class Message(
    val id: MessageId,
    val chatId: ChatId,
    val senderType: SenderType,
    val content: List<MessageContent>,
    val metadata: JsonObject?,
    val createdAt: Instant,
) {
    companion object {
        /**
         * 创建新消息
         */
        fun create(
            chatId: ChatId,
            senderType: SenderType,
            content: List<MessageContent>,
            metadata: JsonObject? = null,
        ): Message {
            return Message(
                id = MessageId(java.util.UUID.randomUUID()),
                chatId = chatId,
                senderType = senderType,
                content = content,
                metadata = metadata,
                createdAt = Clock.System.now(),
            )
        }

        /**
         * 创建纯文本消息
         */
        fun createText(
            chatId: ChatId,
            senderType: SenderType,
            text: String,
            metadata: JsonObject? = null,
        ): Message {
            return create(
                chatId = chatId,
                senderType = senderType,
                content = listOf(MessageContent.Text(text)),
                metadata = metadata,
            )
        }

        /**
         * 从持久化层重建消息
         */
        fun reconstitute(
            id: MessageId,
            chatId: ChatId,
            senderType: SenderType,
            content: List<MessageContent>,
            metadata: JsonObject?,
            createdAt: Instant,
        ): Message = Message(
            id = id,
            chatId = chatId,
            senderType = senderType,
            content = content,
            metadata = metadata,
            createdAt = createdAt,
        )
    }

    /**
     * 获取纯文本内容（拼接所有 Text 类型的 content）
     */
    fun getPlainText(): String {
        return content.filterIsInstance<MessageContent.Text>()
            .joinToString("\n") { it.content }
    }

    /**
     * 是否为纯文本消息
     */
    fun isTextOnly(): Boolean = content.all { it is MessageContent.Text }
}

/**
 * 用于从持久化层重建的工厂方法
 */
fun Message.Companion.reconstitute(
    id: MessageId,
    chatId: ChatId,
    senderType: SenderType,
    content: List<MessageContent>,
    metadata: JsonObject?,
    createdAt: Instant,
): Message = Message(
    id = id,
    chatId = chatId,
    senderType = senderType,
    content = content,
    metadata = metadata,
    createdAt = createdAt,
)
