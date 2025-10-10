package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatEntity(
    @PrimaryKey
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

