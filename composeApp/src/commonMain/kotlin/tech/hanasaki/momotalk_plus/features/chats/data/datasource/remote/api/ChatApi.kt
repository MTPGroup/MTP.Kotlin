package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.*
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.*

interface ChatApi {
    /**
     * 创建聊天会话
     */
    @Headers("Content-Type: application/json")
    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest,
    ): CreateChatResponse

    /**
     * 获取聊天会话列表
     */
    @GET("chats")
    suspend fun getChatList(): GetChatListResponse

    /**
     * 删除聊天会话
     */
    @DELETE("chats/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String)

    /**
     * 更新聊天会话信息
     */
    @PUT("chats/{chatId}")
    suspend fun updateChatInfo(
        @Path("chatId") chatId: String,
        @Body request: UpdateChatInfoRequest,
    ): UpdateChatInfoResponse

    /**
     * 获取聊天会话信息
     */
    @GET("chats/{chatId}")
    suspend fun getChatInfo(@Path("chatId") chatId: String): GetChatInfoResponse

    /**
     * 获取聊天会话中的消息列表
     */
    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int? = null,
    ): GetMessagesResponse

    /**
     * 清空聊天会话中的消息
     */
    @DELETE("chats/{chatId}/messages")
    suspend fun clearChatHistory(@Path("chatId") chatId: String)
}