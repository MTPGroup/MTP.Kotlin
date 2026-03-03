package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tech.hanasaki.momotalk_plus.core.auth.TokenStore
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.MessageContentDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.MessageResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.PagedResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.SendMessageRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ToolCallResultData
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ToolCallStartData
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.UpdateChatNameRequest
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

class ChatRemoteDatasource(
    private val client: HttpClient,
    private val tokenStore: TokenStore,
    private val json: Json,
) {
    suspend fun createChat(request: CreateChatRequest): ApiEnvelope<ChatResponseDto> =
        client.post("chats") {
            setBody(request)
        }.body()

    suspend fun listChats(page: Int, limit: Int): ApiEnvelope<PagedResponseDto<ChatResponseDto>> =
        client.get("chats") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }.body()

    suspend fun getChat(chatId: String): ApiEnvelope<ChatResponseDto> =
        client.get("chats/$chatId").body()

    suspend fun updateChatName(chatId: String, request: UpdateChatNameRequest): ApiEnvelope<Any> =
        client.put("chats/$chatId/name") {
            setBody(request)
        }.body()

    suspend fun deleteChat(chatId: String) {
        client.delete("chats/$chatId")
    }

    suspend fun listMessages(chatId: String, page: Int, limit: Int): ApiEnvelope<PagedResponseDto<MessageResponseDto>> =
        client.get("chats/$chatId/messages") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }.body()

    suspend fun clearMessages(chatId: String) {
        client.delete("chats/$chatId/messages")
    }

    fun sendMessageStream(chatId: String, message: String): Flow<StreamEvent> = flow {
        val accessToken = tokenStore.get()?.accessToken.orEmpty()

        client.sse({
            method = io.ktor.http.HttpMethod.Post
            url {
                path("chats", chatId, "messages")
            }
            contentType(ContentType.Application.Json)
            if (accessToken.isNotBlank()) {
                headers.append(io.ktor.http.HttpHeaders.Authorization, "Bearer $accessToken")
            }
            setBody(
                SendMessageRequest(
                    content = listOf(
                        MessageContentDto(type = "text", content = message),
                    ),
                ),
            )
        }) {
            incoming.collect { event ->
                val type = event.event ?: return@collect
                val data = event.data ?: return@collect
                when (type) {
                    "delta" -> emit(StreamEvent.Delta(data))
                    "tool_call_start" -> {
                        runCatching { json.decodeFromString<ToolCallStartData>(data) }
                            .onSuccess { emit(StreamEvent.ToolCallStart(it.name, json.encodeToString(it.arguments))) }
                    }

                    "tool_call_result" -> {
                        runCatching { json.decodeFromString<ToolCallResultData>(data) }
                            .onSuccess { emit(StreamEvent.ToolCallResult(it.name, it.result)) }
                    }

                    "done" -> emit(StreamEvent.Done(data))
                    "error" -> emit(StreamEvent.Error(data))
                }
            }
        }
    }
}
