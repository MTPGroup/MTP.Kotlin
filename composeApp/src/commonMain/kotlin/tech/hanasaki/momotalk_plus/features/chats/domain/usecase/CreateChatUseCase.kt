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
    ): Result<Unit> = try {
        repository.createChat(
            characterId = characterId,
            name = title,
            description = description,
            avatarUrl = avatarUrl,
            temporary = false,
        )
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}