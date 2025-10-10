package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val role: String, // "user" or "assistant"
    val content: String,
    val type: String, // "text" or "image"
    val createdAt: String,
    val updatedAt: String,
)

