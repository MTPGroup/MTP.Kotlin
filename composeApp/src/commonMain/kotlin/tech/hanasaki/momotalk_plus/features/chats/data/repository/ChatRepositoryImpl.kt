package tech.hanasaki.momotalk_plus.features.chats.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatInfoResponse
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatListResponse
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatMessagesResponse
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.UpdateChatInfoRequest
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.ChatWithCharacter
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val supabase: SupabaseClient,
    private val remoteDatasource: ChatRemoteDatasource,
) : ChatRepository {

    override suspend fun createChat(
        characterId: String,
        title: String,
        description: String?,
        avatarUrl: String?,
    ) {
        supabase.functions.invoke("chats") {
            url { path("chats", "private") }
            method = HttpMethod.Post
            setBody(
                CreateChatRequest(
                    characterId = characterId,
                    title = title,
                    description = description,
                    avatarUrl = avatarUrl,
                )
            )
        }
    }

    override fun getChatList(): Flow<List<Chat>> = flow {
        val response = supabase.functions.invoke("chats") {
            url { path("chats") }
            method = HttpMethod.Get
        }.body<ChatListResponse>()
        emit(response.data.chats)
    }

    override suspend fun deleteChat(chatId: String) {
        supabase.functions.invoke("chats") {
            url { path("chats", chatId) }
            method = HttpMethod.Delete
        }
    }

    override suspend fun updateChatInfo(
        chatId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ) {
        supabase.functions.invoke("chats") {
            url { path("chats", chatId) }
            method = HttpMethod.Patch
            setBody(
                UpdateChatInfoRequest(
                    title = title,
                    description = description,
                    avatarUrl = avatarUrl
                )
            )
        }
    }

    override fun getChatInfo(chatId: String): Flow<ChatWithCharacter> = flow {
        val response = supabase.functions.invoke("chats") {
            url { path("chats", chatId) }
            method = HttpMethod.Get
        }.body<ChatInfoResponse>()
        emit(response.data)
    }

    override fun getChatHistory(chatId: String, limits: Int?): Flow<List<Message>> = flow {
        val response = supabase.functions.invoke("chats") {
            url {
                path("chats", chatId, "messages")
                if (limits != null) {
                    parameters.append("limit", limits.toString())
                }
            }
            method = HttpMethod.Get
        }.body<ChatMessagesResponse>()
        emit(response.data.messages)
    }

    override suspend fun clearChatHistory(chatId: String) {
        supabase.functions.invoke("chats") {
            url { path("chats", chatId, "messages") }
            method = HttpMethod.Delete
        }
    }

    override fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<StreamEvent> {
        return remoteDatasource.sendMessageStream(chatId, message)
    }
}
