package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class DeleteChatUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(chatId: String) = repository.deleteChat(chatId)
}