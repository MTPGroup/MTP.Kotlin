package tech.hanasaki.momotalk_plus.features.chats.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

interface ChatRepository {
    suspend fun createChat(
        characterId: String,
        name: String,
        description: String? = null,
        avatarUrl: String? = null,
        temporary: Boolean = false,
    )

    fun getChatList(page: Int = 1, limit: Int = 20): Flow<List<Chat>>

    suspend fun deleteChat(chatId: String)

    suspend fun updateChatInfo(
        chatId: String,
        name: String,
        description: String? = null,
        avatarUrl: String? = null,
    )

    fun getChatInfo(chatId: String): Flow<ChatWithCharacter>

    fun getChatHistory(chatId: String, page: Int = 1, limit: Int = 50): Flow<List<Message>>

    suspend fun clearChatHistory(chatId: String)

    fun sendMessageStream(chatId: String, message: String): Flow<StreamEvent>
}
