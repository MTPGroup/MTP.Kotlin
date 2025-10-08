package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.BaseRemoteDatasource
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.chats.data.model.*
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamChunk
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

class ChatRemoteDatasource(
    client: HttpClient,
) : BaseRemoteDatasource(client) {
    private val endpoint = "$baseUrl/chats"

    /**
     * 创建聊天会话
     *
     * @param characterId 角色ID
     * @param title 聊天标题
     * @param description 聊天描述
     * @param avatarUrl 聊天头像URL
     *
     * @return IResult<[CreateChatResponse], [AppError]>
     */
    suspend fun createChat(
        characterId: String,
        title: String,
        description: String? = null,
        avatarUrl: String? = null,
    ): IResult<CreateChatResponse, AppError> =
        post<CreateChatResponse>(
            url = endpoint,
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
     * @return IResult<[GetChatListResponse], [AppError]>
     */
    suspend fun getChatList(): IResult<GetChatListResponse, AppError> =
        get<GetChatListResponse>(
            endpoint,
        )

    /**
     * 删除聊天会话
     * @param chatId 聊天ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun deleteChat(chatId: String): IResult<Unit, AppError> =
        delete<Any>(
            "$endpoint/$chatId",
        ).map { }

    /**
     * 更新聊天会话信息
     *
     * @param chatId 聊天ID
     * @param title 聊天标题
     * @param description 聊天描述
     * @param avatarUrl 聊天头像URL
     *
     * @return IResult<[UpdateChatInfoResponse], [AppError]>
     */
    suspend fun updateChatInfo(
        chatId: String,
        title: String,
        description: String? = null,
        avatarUrl: String? = null,
    ): IResult<UpdateChatInfoResponse, AppError> =
        put<UpdateChatInfoResponse>(
            url = "$endpoint/$chatId",
            requestBody = UpdateChatInfoRequest(
                title = title,
                description = description,
                avatarUrl = avatarUrl,
            ),
        )

    /**
     * 获取聊天会话信息
     *
     * @param chatId 聊天ID
     * @return IResult<[GetChatInfoResponse], [AppError]>
     */
    suspend fun getChatInfo(chatId: String): IResult<GetChatInfoResponse, AppError> =
        get<GetChatInfoResponse>(
            "$endpoint/$chatId",
        )

    /**
     * 获取聊天会话中的消息列表
     *
     * @param chatId 聊天ID
     * @param limit 消息数量限制
     *
     * @return IResult<[GetMessagesResponse], [AppError]>
     */
    suspend fun getChatHistory(
        chatId: String,
        limit: Int? = null,
    ): IResult<GetMessagesResponse, AppError> =
        get<GetMessagesResponse>(
            "$endpoint/$chatId/messages${if (limit != null) "?limit=$limit" else ""}",
        )

    /**
     * 清空聊天会话中的消息
     *
     * @param chatId 聊天ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun clearChatHistory(chatId: String): IResult<Unit, AppError> =
        delete<Any>(
            "$endpoint/$chatId/messages",
        ).map { }

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
    ): Flow<IResult<StreamEvent, AppError>> = flow {
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