package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.ChatResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.CreateChatRequest
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.MessageResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.PagedResponseDto
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto.UpdateChatNameRequest

interface ChatApi {
    @Headers("Content-Type: application/json")
    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequest): ApiEnvelope<ChatResponseDto>

    @GET("chats")
    suspend fun getChatList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<PagedResponseDto<ChatResponseDto>>

    @GET("chats/{chatId}")
    suspend fun getChatInfo(@Path("chatId") chatId: String): ApiEnvelope<ChatResponseDto>

    @PUT("chats/{chatId}/name")
    suspend fun updateChatName(
        @Path("chatId") chatId: String,
        @Body request: UpdateChatNameRequest,
    ): ApiEnvelope<Any>

    @DELETE("chats/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String)

    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): ApiEnvelope<PagedResponseDto<MessageResponseDto>>

    @DELETE("chats/{chatId}/messages")
    suspend fun clearChatHistory(@Path("chatId") chatId: String)
}
