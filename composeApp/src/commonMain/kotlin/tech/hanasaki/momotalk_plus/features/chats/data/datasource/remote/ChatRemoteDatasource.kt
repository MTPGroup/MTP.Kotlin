package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.utils.Constants
import tech.hanasaki.momotalk_plus.features.chats.data.model.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.model.GetChatListResponse
import tech.hanasaki.momotalk_plus.features.chats.data.model.GetMessagesResponse
import tech.hanasaki.momotalk_plus.features.chats.data.model.UpdateChatInfoRequest
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamChunk
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ChatRemoteDatasource(
    private val client: HttpClient,
) {
    private val endpoint = "${Constants.BASE_URL}/chat"

    private suspend inline fun <reified R : Any> postAuthRequest(
        url: String,
        requestBody: Any? = null,
        headers: Headers? = null,
    ): IResult<R, AppError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                if (requestBody != null) {
                    setBody(requestBody)
                }
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body()
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(AppError("发生错误: $errorBody"))
            } catch (_: Exception) {
                IResult.Error(
                    AppError(
                        "客户端请求失败，无法解析错误信息。",
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                AppError(
                    "服务器响应错误: ${e.response.status.description}",
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                AppError(
                    "重定向错误: ${e.response.status.description}",
                )
            )
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 创建聊天会话
     *
     * @param characterId 角色ID
     * @param title 聊天标题
     * @param description 聊天描述
     * @param avatarUrl 聊天头像URL
     *
     */
    suspend fun createChat(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): IResult<Unit, AppError> = postAuthRequest(
        url = "$endpoint/create",
        requestBody = CreateChatRequest(
            characterId = characterId,
            title = title,
            description = description,
            avatarUrl = avatarUrl,
        ),
    )

    /**
     * 获取聊天会话列表
     *
     * @return IResult<List<Chat>, AppError>
     */
    suspend fun getChatList(): IResult<GetChatListResponse, AppError> =
        postAuthRequest<GetChatListResponse>(
            url = "$endpoint/list"
        )

    /**
     * 删除聊天会话
     * @param chatId 聊天ID
     */
    suspend fun deleteChat(chatId: String): IResult<Unit, AppError> {
        return try {
            client.delete(
                "$endpoint/delete/$chatId",
            )
            IResult.Success(Unit)
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 更新聊天会话信息
     *
     * @param chatId 聊天ID
     * @param title 聊天标题
     * @param description 聊天描述
     * @param avatarUrl 聊天头像URL
     */
    suspend fun updateChatInfo(
        chatId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): IResult<Unit, AppError> = postAuthRequest(
        url = "$endpoint/update/$chatId",
        requestBody = UpdateChatInfoRequest(
            title = title,
            description = description,
            avatarUrl = avatarUrl,
        ),
    )

    /**
     * 获取聊天会话中的消息列表
     *
     * @param chatId 聊天ID
     *
     * @return IResult<GetMessagesResponse, AppError>
     */
    @OptIn(ExperimentalTime::class)
    suspend fun getChatHistory(
        chatId: String,
        limit: Int? = null,
    ): IResult<GetMessagesResponse, AppError> {
        return try {
            val response: GetMessagesResponse = client.get(
                "$endpoint/$chatId/message/list",
            ) {
                parameters {
                    if (limit != null) {
                        parameter("limit", limit)
                    }
                    parameter(
                        "before",
                        Clock.System.now().toString()
                    )
                }
            }.body()
            IResult.Success(response)
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 清空聊天会话中的消息
     *
     * @param chatId 聊天ID
     *
     * @return IResult<Unit, AppError>
     */
    suspend fun clearChatHistory(chatId: String): IResult<Unit, AppError> {
        return try {
            client.delete(
                "$endpoint/$chatId/message/delete",
            )
            IResult.Success(Unit)
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 发送消息并以流式方式接收响应
     *
     * @param chatId 聊天ID
     * @param message 消息内容
     *
     * @return Flow<IResult<StreamEvent, AppError>>
     */
    fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<IResult<StreamEvent, AppError>> = flow {
        try {
            client.sse(
                urlString = "$endpoint/$chatId/message/send-stream",
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

                        streamEvent?.let { emit(IResult.Success(it)) }
                    } catch (e: Exception) {
                        emit(IResult.Error(AppError("解析流式数据失败: ${e.message}")))
                    }
                }
            }
        } catch (e: Exception) {
            emit(IResult.Error(AppError(e.message ?: "流式请求失败")))
        }
    }
}