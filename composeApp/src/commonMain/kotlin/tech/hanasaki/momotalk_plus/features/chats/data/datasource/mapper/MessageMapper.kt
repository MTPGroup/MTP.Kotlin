package tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper

import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.MessageEntity
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageSender
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageSenderRole
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageType

object MessageMapper {
    fun MessageEntity.toMessage(): Message {
        return Message(
            id = id,
            sender = MessageSender(
                name = senderName,
                avatar = senderAvatarUrl
            ),
            chatId = chatId,
            role = when (role) {
                "user" -> MessageSenderRole.USER
                "ai" -> MessageSenderRole.AI
                else -> MessageSenderRole.USER
            },
            content = content,
            type = when (type) {
                "image" -> MessageType.IMAGE
                else -> MessageType.TEXT
            },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            chatId = chatId,
            senderName = sender.name,
            senderAvatarUrl = sender.avatar,
            role = when (role) {
                MessageSenderRole.USER -> "user"
                MessageSenderRole.AI -> "assistant"
            },
            content = content,
            type = when (type) {
                MessageType.TEXT -> "text"
                MessageType.IMAGE -> "image"
            },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

