package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.SerialName
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
    @SerialName("text")
    data class Text(
        val content: String,
    ) : MessageContent()

    @Serializable
    @SerialName("image")
    data class Image(
        val url: String,
        val alt: String? = null,
    ) : MessageContent()

    @Serializable
    @SerialName("file")
    data class File(
        val url: String,
        val fileName: String,
        val fileSize: Long? = null,
    ) : MessageContent()

    @Serializable
    @SerialName("code")
    data class Code(
        val code: String,
        val language: String? = null,
    ) : MessageContent()

    @Serializable
    @SerialName("audio")
    data class Audio(
        val url: String,
        val mimeType: String? = null,
    ) : MessageContent()

    @Serializable
    @SerialName("video")
    data class Video(
        val url: String,
        val mimeType: String? = null,
    ) : MessageContent()

    @Serializable
    @SerialName("pdf")
    data class Pdf(
        val url: String,
        val fileName: String? = null,
    ) : MessageContent()
}
