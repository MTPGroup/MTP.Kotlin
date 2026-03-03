package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatWithCharacter(
    val id: String,
    val characterId: String,
    val title: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val createdAt: String,
    val updatedAt: String,
    val characterName: String,
    val characterAvatar: String?,
)
