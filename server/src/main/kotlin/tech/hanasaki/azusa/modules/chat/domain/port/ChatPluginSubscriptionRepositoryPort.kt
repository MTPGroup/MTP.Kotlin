package tech.hanasaki.azusa.modules.chat.domain.port

import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscriptionId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId

/**
 * ChatPluginSubscription 仓储接口
 */
interface ChatPluginSubscriptionRepositoryPort {
    /**
     * 根据 ID 查找订阅
     */
    suspend fun findById(id: ChatPluginSubscriptionId): ChatPluginSubscription?

    /**
     * 根据聊天 ID 查找所有订阅
     */
    suspend fun findByChatId(chatId: ChatId): List<ChatPluginSubscription>

    /**
     * 根据聊天 ID 查找启用的订阅
     */
    suspend fun findEnabledByChatId(chatId: ChatId): List<ChatPluginSubscription>

    /**
     * 根据聊天 ID 和插件 ID 查找订阅
     */
    suspend fun findByChatIdAndPluginId(chatId: ChatId, pluginId: PluginId): ChatPluginSubscription?

    /**
     * 保存订阅（创建或更新）
     */
    suspend fun save(subscription: ChatPluginSubscription)

    /**
     * 根据 ID 删除订阅
     */
    suspend fun deleteById(id: ChatPluginSubscriptionId)

    /**
     * 根据聊天 ID 删除所有订阅（级联删除）
     */
    suspend fun deleteByChatId(chatId: ChatId)
}
