package tech.hanasaki.azusa.modules.chat.domain.port

import tech.hanasaki.azusa.modules.chat.domain.model.ChatMember
import tech.hanasaki.azusa.modules.chat.domain.model.ChatMemberId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId

/**
 * ChatMember 仓储接口
 */
interface ChatMemberRepositoryPort {
    /**
     * 根据 ID 查找成员
     */
    suspend fun findById(id: ChatMemberId): ChatMember?

    /**
     * 根据聊天 ID 查找所有成员
     */
    suspend fun findByChatId(chatId: ChatId): List<ChatMember>

    /**
     * 保存成员（创建或更新）
     */
    suspend fun save(member: ChatMember)

    /**
     * 根据 ID 删除成员
     */
    suspend fun deleteById(id: ChatMemberId)

    /**
     * 根据聊天 ID 删除所有成员（级联删除）
     */
    suspend fun deleteByChatId(chatId: ChatId)
}
