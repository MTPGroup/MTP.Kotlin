package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class SendMessageStreamUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(
        chatId: String,
        message: String,
    ): Flow<StreamEvent> {
        return chatRepository.sendMessageStream(
            chatId = chatId,
            message = message,
        )
    }
}