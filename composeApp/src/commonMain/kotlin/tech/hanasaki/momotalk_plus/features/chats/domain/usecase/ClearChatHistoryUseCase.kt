package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class ClearChatHistoryUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(chatId: String): Result<Unit> = try {
        chatRepository.clearChatHistory(chatId)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}