package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class GetChatHistoryUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(chatId: String, limits: Int? = null): Flow<List<Message>> =
        chatRepository.getChatHistory(chatId = chatId, page = 1, limit = limits ?: 50)
}
