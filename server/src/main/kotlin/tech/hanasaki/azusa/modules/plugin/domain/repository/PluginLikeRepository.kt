package tech.hanasaki.azusa.modules.plugin.domain.repository

import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface PluginLikeRepository {
    /**
     * 检查用户是否点赞了插件
     */
    suspend fun exists(userId: UserId, pluginId: PluginId): Boolean

    /**
     * 点赞
     */
    suspend fun like(userId: UserId, pluginId: PluginId)

    /**
     * 取消点赞
     */
    suspend fun unlike(userId: UserId, pluginId: PluginId)
}
