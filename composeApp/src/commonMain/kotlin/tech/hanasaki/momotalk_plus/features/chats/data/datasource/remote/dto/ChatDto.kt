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
data class GetChatInfoResponse(
    val success: Boolean,
    val data: ChatWithCharacter,
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
    val description: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class UpdateChatInfoResponse(
    val success: Boolean,
)
