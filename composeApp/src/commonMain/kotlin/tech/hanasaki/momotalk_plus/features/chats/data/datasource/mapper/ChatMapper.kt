package tech.hanasaki.momotalk_plus.features.chats.data.datasource.mapper

import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.ChatEntity
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat

object ChatMapper {
    fun ChatEntity.toChat(): Chat {
        return Chat(
            id = id,
            creatorId = creatorId,
            characterId = characterId,
            title = title,
            description = description,
            avatarUrl = avatarUrl,
            lastMessage = lastMessage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Chat.toEntity(): ChatEntity {
        return ChatEntity(
            id = id,
            creatorId = creatorId,
            characterId = characterId,
            title = title,
            description = description,
            avatarUrl = avatarUrl,
            lastMessage = lastMessage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

