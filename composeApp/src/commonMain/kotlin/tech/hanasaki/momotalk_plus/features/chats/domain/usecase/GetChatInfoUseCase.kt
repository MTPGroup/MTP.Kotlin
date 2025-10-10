package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class GetChatInfoUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(chatId: String): Flow<ChatWithCharacter> = repository.getChatInfo(chatId)
}