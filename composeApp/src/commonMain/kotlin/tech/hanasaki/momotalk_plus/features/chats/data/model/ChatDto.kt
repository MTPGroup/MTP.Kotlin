package tech.hanasaki.momotalk_plus.features.chats.data.model

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat

@Serializable
data class CreateChatRequest(
    val characterId: String,
    val title: String,
    val description: String,
    val avatarUrl: String,
)

@Serializable
data class GetChatListData(
    val chats: List<Chat>,
)

@Serializable
data class GetChatListResponse(
    val success: Boolean,
    val data: GetChatListData,
)

@Serializable
data class UpdateChatInfoRequest(
    val title: String,
    val description: String,
    val avatarUrl: String,
)