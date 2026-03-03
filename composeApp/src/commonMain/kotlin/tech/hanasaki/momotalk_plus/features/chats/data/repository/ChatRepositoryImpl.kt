package tech.hanasaki.momotalk_plus.features.chats.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.hanasaki.momotalk_plus.core.network.AppErrorException
import tech.hanasaki.momotalk_plus.core.network.AppResult
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.callApi
import tech.hanasaki.momotalk_plus.core.network.callRawApi
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.MessageResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.UpdateChatNameRequest
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageSender
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageSenderRole
import tech.hanasaki.momotalk_plus.features.chats.domain.model.MessageType
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val remote: ChatRemoteDatasource,
    private val errorMapper: NetworkErrorMapper,
) : ChatRepository {

    override suspend fun createChat(
        characterId: String,
        name: String,
        description: String?,
        avatarUrl: String?,
        temporary: Boolean,
    ) {
        val result = callApi(errorMapper) {
            remote.createChat(
                CreateChatRequest(
                    characterId = characterId,
                    name = name.ifBlank { null },
                    temporary = temporary,
                ),
            )
        }
        result.throwIfFailure()
    }

    override fun getChatList(page: Int, limit: Int): Flow<List<Chat>> = flow {
        when (val result = callApi(errorMapper) { remote.listChats(page, limit) }) {
            is AppResult.Success -> emit(result.data.items.map { it.toDomainChat() })
            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override suspend fun deleteChat(chatId: String) {
        val result = callRawApi(errorMapper) { remote.deleteChat(chatId) }
        result.throwIfFailure()
    }

    override suspend fun updateChatInfo(
        chatId: String,
        name: String,
        description: String?,
        avatarUrl: String?,
    ) {
        val result = callApi(errorMapper) {
            remote.updateChatName(chatId, UpdateChatNameRequest(name = name.ifBlank { null }))
        }
        result.throwIfFailure()
    }

    override fun getChatInfo(chatId: String): Flow<ChatWithCharacter> = flow {
        when (val result = callApi(errorMapper) { remote.getChat(chatId) }) {
            is AppResult.Success -> emit(result.data.toDomainChatWithCharacter())
            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override fun getChatHistory(chatId: String, page: Int, limit: Int): Flow<List<Message>> = flow {
        when (val result = callApi(errorMapper) { remote.listMessages(chatId, page, limit) }) {
            is AppResult.Success -> emit(result.data.items.map { it.toDomainMessage(chatId) })
            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override suspend fun clearChatHistory(chatId: String) {
        val result = callRawApi(errorMapper) { remote.clearMessages(chatId) }
        result.throwIfFailure()
    }

    override fun sendMessageStream(chatId: String, message: String): Flow<StreamEvent> =
        remote.sendMessageStream(chatId, message)
}

private fun ChatResponseDto.toDomainChat(): Chat = Chat(
    id = id,
    creatorId = "",
    characterId = characterId.orEmpty(),
    title = name ?: "未命名会话",
    description = "",
    avatarUrl = null,
    lastMessage = lastMessage,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun ChatResponseDto.toDomainChatWithCharacter(): ChatWithCharacter = ChatWithCharacter(
    id = id,
    characterId = characterId.orEmpty(),
    title = name ?: "未命名会话",
    avatarUrl = null,
    lastMessage = lastMessage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    characterName = name ?: "对话角色",
    characterAvatar = null,
)

private fun MessageResponseDto.toDomainMessage(chatId: String): Message {
    val textContent = content
        .joinToString("\n") { item ->
            when (item.type) {
                "text" -> item.content.orEmpty()
                "image", "audio", "video", "pdf", "file" -> item.url ?: item.fileName.orEmpty()
                "code" -> item.content.orEmpty()
                else -> item.content.orEmpty()
            }
        }
        .ifBlank { "[空消息]" }

    val role = if (senderType.equals("USER", ignoreCase = true)) {
        MessageSenderRole.USER
    } else {
        MessageSenderRole.AI
    }

    return Message(
        id = id,
        sender = MessageSender(
            name = if (role == MessageSenderRole.USER) "我" else "AI",
            avatar = null,
        ),
        chatId = chatId,
        role = role,
        content = textContent,
        type = MessageType.TEXT,
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}

private fun AppResult<*>.throwIfFailure() {
    if (this is AppResult.Failure) throw AppErrorException(error)
}
