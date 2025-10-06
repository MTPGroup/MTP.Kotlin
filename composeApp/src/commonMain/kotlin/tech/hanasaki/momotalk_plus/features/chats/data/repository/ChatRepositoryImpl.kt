package tech.hanasaki.momotalk_plus.features.chats.data.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val remoteDatasource: ChatRemoteDatasource,
) : ChatRepository {
    override suspend fun createChat(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): IResult<Unit, AppError> =
        remoteDatasource.createChat(
            characterId,
            title,
            description,
            avatarUrl,
        )

    override suspend fun getChatList(): IResult<List<Chat>, AppError> =
        remoteDatasource.getChatList().map { getChatListResponse ->
            getChatListResponse.data.chats
        }

    override suspend fun deleteChat(chatId: String): IResult<Unit, AppError> =
        remoteDatasource.deleteChat(chatId)

    override suspend fun updateChatInfo(
        chatId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): IResult<Unit, AppError> =
        remoteDatasource.updateChatInfo(
            chatId,
            title,
            description,
            avatarUrl,
        )

    override suspend fun getChatHistory(
        chatId: String,
        limits: Int?,
    ): IResult<List<Message>, AppError> =
        remoteDatasource.getChatHistory(chatId, limits).map { getChatHistoryResponse ->
            getChatHistoryResponse.data.messages
        }

    override suspend fun clearChatHistory(chatId: String): IResult<Unit, AppError> =
        remoteDatasource.clearChatHistory(chatId)

    override fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<IResult<StreamEvent, AppError>> =
        remoteDatasource.sendMessageStream(
            chatId,
            message,
        )
}