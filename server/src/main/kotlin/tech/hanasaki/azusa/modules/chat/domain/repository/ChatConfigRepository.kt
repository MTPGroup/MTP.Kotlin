package tech.hanasaki.azusa.modules.chat.domain.repository

import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfigId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId

/**
 * ChatConfig 仓储接口
 */
interface ChatConfigRepository {
    /**
     * 根据 ID 查找配置
     */
    suspend fun findById(id: ChatConfigId): ChatConfig?

    /**
     * 根据聊天 ID 查找配置
     */
    suspend fun findByChatId(chatId: ChatId): ChatConfig?

    /**
     * 保存配置（创建或更新）
     */
    suspend fun save(config: ChatConfig)

    /**
     * 根据 ID 删除配置
     */
    suspend fun deleteById(id: ChatConfigId)

    /**
     * 根据聊天 ID 删除配置（级联删除）
     */
    suspend fun deleteByChatId(chatId: ChatId)
}
