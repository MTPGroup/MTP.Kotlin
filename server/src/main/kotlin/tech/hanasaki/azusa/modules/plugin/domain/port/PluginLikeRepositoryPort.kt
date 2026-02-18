package tech.hanasaki.azusa.modules.plugin.domain.port

import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface PluginLikeRepositoryPort {
    suspend fun exists(userId: UserId, pluginId: PluginId): Boolean
    suspend fun like(userId: UserId, pluginId: PluginId)
    suspend fun unlike(userId: UserId, pluginId: PluginId)
}
