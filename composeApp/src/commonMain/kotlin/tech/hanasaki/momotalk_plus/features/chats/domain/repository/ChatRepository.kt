package tech.hanasaki.momotalk_plus.features.chats.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Chat
import tech.hanasaki.momotalk_plus.features.chats.domain.model.Message
import tech.hanasaki.momotalk_plus.features.chats.domain.model.StreamEvent

interface ChatRepository {
    /**
     * 创建聊天会话
     *
     * @param characterId 角色ID
     * @param title 聊天标题
     * @param description 聊天描述
     * @param avatarUrl 聊天头像URL
     */
    suspend fun createChat(
        characterId: String,
        title: String,
        description: String,
        avatarUrl: String,
    ): IResult<Unit, AppError>

    /**
     * 获取聊天会话列表
     */
    suspend fun getChatList(): IResult<List<Chat>, AppError>

    /**
     * 删除聊天会话
     *
     * @param chatId 聊天ID
     */
    suspend fun deleteChat(chatId: String): IResult<Unit, AppError>

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
    ): IResult<Unit, AppError>

    /**
     * 获取单个聊天会话历史记录
     *
     * @param chatId 聊天ID
     * @param limits 获取消息数量限制
     *
     */
    suspend fun getChatHistory(chatId: String, limits: Int? = null): IResult<List<Message>, AppError>

    /**
     * 清除聊天会话历史记录
     *
     * @param chatId 聊天ID
     */
    suspend fun clearChatHistory(chatId: String): IResult<Unit, AppError>

    /**
     * 流式获取聊天回复
     * @param chatId 聊天ID
     * @param message 用户消息
     */
    fun sendMessageStream(
        chatId: String,
        message: String,
    ): Flow<IResult<StreamEvent, AppError>>
}