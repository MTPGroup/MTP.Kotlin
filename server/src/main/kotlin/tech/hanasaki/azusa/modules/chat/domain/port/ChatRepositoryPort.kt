package tech.hanasaki.azusa.modules.chat.domain.port

import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId

/**
 * Chat 仓储接口
 */
interface ChatRepositoryPort {
    /**
     * 根据 ID 查找聊天
     */
    suspend fun findById(id: ChatId): Chat?

    /**
     * 保存聊天（创建或更新）
     */
    suspend fun save(chat: Chat)

    /**
     * 根据 ID 删除聊天
     */
    suspend fun deleteById(id: ChatId)

    /**
     * 获取用户的聊天列表（分页）
     */
    suspend fun findByOwnerIdPaged(ownerId: UserId, page: Int, limit: Int): PageResult<Chat>
}
