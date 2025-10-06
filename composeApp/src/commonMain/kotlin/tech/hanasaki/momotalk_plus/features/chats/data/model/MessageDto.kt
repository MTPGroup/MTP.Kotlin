package tech.hanasaki.momotalk_plus.features.chats.data.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message

@Serializable
data class GetMessagesData(
    val messages: List<Message>,
)

@Serializable
data class GetMessagesResponse(
    val success: Boolean,
    val data: GetMessagesData,
)