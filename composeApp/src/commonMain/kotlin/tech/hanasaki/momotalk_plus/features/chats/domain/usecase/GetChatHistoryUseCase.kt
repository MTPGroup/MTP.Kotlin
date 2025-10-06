package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class GetChatHistoryUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(chatId: String, limits: Int? = null) =
        chatRepository.getChatHistory(chatId, limits)
}