package tech.hanasaki.momotalk_plus.features.chats.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String,
    val creatorId: String,
    val characterId: String,
    val title: String,
    val description: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val createdAt: String,
    val updatedAt: String,
)
