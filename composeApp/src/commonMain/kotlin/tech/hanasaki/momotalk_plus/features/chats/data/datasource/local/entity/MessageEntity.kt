package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity

data class MessageEntity(
    val id: String,
    val chatId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val role: String,
    val content: String,
    val type: String,
    val createdAt: String,
    val updatedAt: String,
)
