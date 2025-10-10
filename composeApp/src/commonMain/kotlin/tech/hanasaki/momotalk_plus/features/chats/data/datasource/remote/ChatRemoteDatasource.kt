package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamChunk
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

class ChatRemoteDatasource(
    private val client: HttpClient,
) {
    private val endpoint = "http://localhost:3001/api/chats"

    /**
     * 发送消息并以流式方式接收响应
     *
     * @param chatId 聊天ID
     * @param message 消息内容
     *
     * @return Flow<IResult<[StreamEvent], [AppError]>>
     */
    fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<StreamEvent> = flow {
        try {
            client.sse(
                urlString = "$endpoint/$chatId/messages/stream",
                request = {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("message" to message))
                }
            ) {
                incoming.collect { event ->
                    try {
                        val chunk = Json.decodeFromString<StreamChunk>(event.data ?: "")

                        val streamEvent = when (chunk.type) {
                            "reflection_chunk" -> StreamEvent.ReflectionChunk(chunk.content ?: "")
                            "token" -> StreamEvent.Token(chunk.content ?: "")
                            "error" -> StreamEvent.Error(chunk.content ?: "")
                            "final" -> StreamEvent.Final
                            else -> null
                        }

                        streamEvent?.let { emit(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}