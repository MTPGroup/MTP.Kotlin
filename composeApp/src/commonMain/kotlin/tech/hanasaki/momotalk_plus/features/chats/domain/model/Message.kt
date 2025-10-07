package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class MessageType {
    @SerialName("text")
    TEXT,

    @SerialName("image")
    IMAGE,
}

@Serializable
data class Message(
    val id: String,
    val sender: MessageSender,
    val chatId: String,
    val role: MessageSenderRole,
    val content: String,
    val type: MessageType,
    val createdAt: String,
    val updatedAt: String,
    val isStreaming: Boolean = false,
)
