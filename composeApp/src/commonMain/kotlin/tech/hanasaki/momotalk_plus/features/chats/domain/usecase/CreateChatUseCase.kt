package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class CreateChatUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ) = repository.createChat(
        characterId = characterId,
        title = title,
        description = description,
        avatarUrl = avatarUrl,
    )
}