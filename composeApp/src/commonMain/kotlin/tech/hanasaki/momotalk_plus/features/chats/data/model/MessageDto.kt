package tech.hanasaki.momotalk_plus.features.chats.data.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message

@Serializable
data class GetMessagesData(
    val messages: List<Message>,
    val next: String?,
)

@Serializable
data class GetMessagesResponse(
    val success: Boolean,
    val message: String,
    val data: GetMessagesData,
)