package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageSender(
    val name: String,
    val avatar: String?,
)

@Serializable
enum class MessageSenderRole {
    @SerialName("user")
    USER,

    @SerialName("ai")
    AI,
}
