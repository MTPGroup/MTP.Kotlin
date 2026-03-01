package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import tech.hanasaki.momotalk_plus.core.auth.TokenStore
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamChunk
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

class ChatRemoteDatasource(
    private val client: HttpClient,
    private val tokenStore: TokenStore,
) {
    // Supabase Edge Function URL
    private val endpoint = "http://localhost:8000/functions/v1/chats"

    /**
     * 发送消息并以流式方式接收响应
     *
     * @param chatId 聊天ID
     * @param message 消息内容
     */
    fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<StreamEvent> = flow {
        try {
            val accessToken = tokenStore.get()?.accessToken.orEmpty()
            
            client.sse(
                urlString = "$endpoint/$chatId/messages/stream",
                request = {
                    method = HttpMethod.Post
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(
                        buildJsonObject {
                            putJsonArray("message") {
                                add(
                                    buildJsonObject {
                                        put("type", "text")
                                        put("text", message)
                                    }
                                )
                            }
                        }
                    )
                }
            ) {
                incoming.collect { event ->
                    try {
                        val data = event.data
                        if (!data.isNullOrBlank()) {
                            // The guide doesn't specify the exact chunk format for SSE, assuming it matches StreamChunk or similar
                            // If the chunk is just raw text or JSON, we need to parse it.
                            // Assuming JSON format similar to previous implementation for now.
                            // If it's raw text, we might need to adjust.
                            // Let's assume it returns JSON with type and content.
                            
                            // Note: SSE data often comes as "data: {...}" lines. Ktor SSE plugin handles parsing "data: " prefix.
                            // We just need to parse the JSON content.
                            
                            val chunk = Json.decodeFromString<StreamChunk>(data)

                            val streamEvent = when (chunk.type) {
                                "reflection_chunk" -> StreamEvent.ReflectionChunk(chunk.content ?: "")
                                "token" -> StreamEvent.Token(chunk.content ?: "")
                                "error" -> StreamEvent.Error(chunk.content ?: "")
                                "final" -> StreamEvent.Final
                                else -> null
                            }

                            streamEvent?.let { emit(it) }
                        }
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