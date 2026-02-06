package tech.hanasaki.azusa.modules.chat.domain.repository

import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId

/**
 * Message 仓储接口
 */
interface MessageRepository {
    /**
     * 保存消息
     */
    suspend fun save(message: Message)

    /**
     * 根据聊天 ID 获取消息列表（分页）
     */
    suspend fun findByChatIdPaged(chatId: ChatId, page: Int, limit: Int): PageResult<Message>

    /**
     * 根据聊天 ID 获取所有消息（用于聊天历史）
     */
    suspend fun findByChatId(chatId: ChatId): List<Message>

    /**
     * 根据消息 ID 删除消息
     */
    suspend fun deleteById(id: MessageId)

    /**
     * 根据聊天 ID 删除所有消息（级联删除）
     */
    suspend fun deleteByChatId(chatId: ChatId)
}
