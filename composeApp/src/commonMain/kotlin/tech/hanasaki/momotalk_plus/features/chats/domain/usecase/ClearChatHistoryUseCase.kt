package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class ClearChatHistoryUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(chatId: String) =
        chatRepository.clearChatHistory(chatId)
}