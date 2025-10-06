package tech.hanasaki.momotalk_plus.features.chats.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class SendMessageStreamUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(
        chatId: String,
        message: String,
    ): Flow<IResult<StreamEvent, AppError>> {
        return chatRepository.sendMessageStream(
            chatId = chatId,
            message = message,
        )
    }
}