package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class UpdateChatUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(
        chatId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): Result<Unit> = try {
        repository.updateChatInfo(
            chatId = chatId,
            title = title,
            description = description,
            avatarUrl = avatarUrl,
        )
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}