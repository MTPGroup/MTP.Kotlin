package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class MessageId(val value: Uuid)

/**
 * Message 实体 - 表示聊天中的一条消息
 */
data class Message(
    val id: MessageId,
    val chatId: ChatId,
    val senderType: SenderType,
    val senderId: Uuid,
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
            senderId: Uuid,
            content: List<MessageContent>,
            metadata: JsonObject? = null,
        ): Message {
            return Message(
                id = MessageId(Uuid.random()),
                chatId = chatId,
                senderType = senderType,
                senderId = senderId,
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
            senderId: Uuid,
            text: String,
            metadata: JsonObject? = null,
        ): Message {
            return create(
                chatId = chatId,
                senderType = senderType,
                senderId = senderId,
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
            senderId: Uuid,
            content: List<MessageContent>,
            metadata: JsonObject?,
            createdAt: Instant,
        ): Message = Message(
            id = id,
            chatId = chatId,
            senderType = senderType,
            senderId = senderId,
            content = content,
            metadata = metadata,
            createdAt = createdAt,
        )
    }

    /**
     * 获取纯文本内容（拼接所有 Text 类型的 content）
     */
    fun getPlainText(): String {
        return content.joinToString("\n") { part ->
            when (part) {
                is MessageContent.Text -> part.content
                is MessageContent.Image -> "[图片: ${part.alt ?: part.url}]"
                is MessageContent.File -> "[文件: ${part.fileName}]"
                is MessageContent.Code -> "```${part.language ?: ""}\n${part.code}\n```"
                is MessageContent.Audio -> "[音频: ${part.url}]"
                is MessageContent.Video -> "[视频: ${part.url}]"
                is MessageContent.Pdf -> "[PDF: ${part.fileName ?: part.url}]"
            }
        }
    }

    /**
     * 是否为纯文本消息
     */
    fun isTextOnly(): Boolean = content.all { it is MessageContent.Text }
}