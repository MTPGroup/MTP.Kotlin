package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message

@Serializable
data class ChatMessagesData(
    val messages: List<Message>,
    val next: String?,
)

@Serializable
data class ChatMessagesResponse(
    val success: Boolean,
    val message: String,
    val data: ChatMessagesData,
)