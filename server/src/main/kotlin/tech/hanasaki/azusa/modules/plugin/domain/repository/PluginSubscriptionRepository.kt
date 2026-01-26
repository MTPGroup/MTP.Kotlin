package tech.hanasaki.azusa.modules.plugin.domain.repository

import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSubscription
import tech.hanasaki.azusa.shared.domain.model.PluginId
import tech.hanasaki.azusa.shared.domain.model.UserId

interface PluginSubscriptionRepository {
    /**
     * 查询用户订阅的插件
     */
    suspend fun findByUserId(userId: UserId): List<PluginSubscription>

    /**
     * 查询用户订阅的已启用插件
     */
    suspend fun findActiveByUserId(userId: UserId): List<PluginSubscription>

    /**
     * 查询用户对某插件的订阅
     */
    suspend fun findByUserIdAndPluginId(userId: UserId, pluginId: PluginId): PluginSubscription?

    /**
     * 订阅插件
     */
    suspend fun subscribe(userId: UserId, pluginId: PluginId, isActive: Boolean = false)

    /**
     * 取消订阅
     */
    suspend fun unsubscribe(userId: UserId, pluginId: PluginId)

    /**
     * 更新订阅状态（启用/禁用）
     */
    suspend fun updateActiveStatus(userId: UserId, pluginId: PluginId, isActive: Boolean)
}
