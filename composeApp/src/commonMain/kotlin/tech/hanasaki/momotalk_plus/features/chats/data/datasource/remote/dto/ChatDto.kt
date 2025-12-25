package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter

@Serializable
data class CreateChatRequest(
    val characterId: String,
    val title: String,
    val description: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class CreateChatResponse(
    val message: String,
)

@Serializable
data class ChatInfoResponse(
    val success: Boolean,
    val data: ChatWithCharacter,
)

@Serializable
data class ChatListData(
    val chats: List<Chat>,
)

@Serializable
data class ChatListResponse(
    val success: Boolean,
    val data: ChatListData,
)

@Serializable
data class UpdateChatInfoRequest(
    val title: String,
    val description: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class UpdateChatInfoResponse(
    val success: Boolean,
)
