package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class GetChatsUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(): Flow<List<Chat>> = repository.getChatList()
}