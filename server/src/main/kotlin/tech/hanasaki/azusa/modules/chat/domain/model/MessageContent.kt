package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable

/**
 * 消息发送者类型枚举
 */
enum class SenderType {
    USER,
    CHARACTER,
}

/**
 * 消息内容 sealed class - 支持多种消息类型
 */
@Serializable
sealed class MessageContent {
    @Serializable
    data class Text(
        val content: String,
    ) : MessageContent()

    @Serializable
    data class Image(
        val url: String,
        val alt: String? = null,
    ) : MessageContent()

    @Serializable
    data class File(
        val url: String,
        val fileName: String,
        val fileSize: Long? = null,
    ) : MessageContent()

    @Serializable
    data class Code(
        val code: String,
        val language: String? = null,
    ) : MessageContent()
}
