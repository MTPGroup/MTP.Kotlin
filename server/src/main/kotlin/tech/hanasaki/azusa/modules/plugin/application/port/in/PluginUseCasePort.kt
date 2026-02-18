package tech.hanasaki.azusa.modules.plugin.application.port.`in`

import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface PluginUseCasePort {
    suspend fun listApprovedPlugins(page: Int, limit: Int): PageResult<Plugin>
    suspend fun searchPlugins(query: String, page: Int, limit: Int): PageResult<Plugin>
    suspend fun listPendingPlugins(page: Int, limit: Int): PageResult<Plugin>
    suspend fun listMyPlugins(userId: UserId, page: Int, limit: Int): PageResult<Plugin>
    suspend fun getPlugin(pluginId: PluginId): Plugin
    suspend fun createPlugin(authorId: UserId, name: String, description: String, version: String, schema: PluginSchema, code: String): Plugin
    suspend fun updatePlugin(userId: UserId, pluginId: PluginId, name: String, description: String, version: String, schema: PluginSchema, code: String): Plugin
    suspend fun deletePlugin(userId: UserId, pluginId: PluginId)
    suspend fun approvePlugin(pluginId: PluginId): Plugin
    suspend fun rejectPlugin(pluginId: PluginId): Plugin
    suspend fun likePlugin(userId: UserId, pluginId: PluginId)
    suspend fun unlikePlugin(userId: UserId, pluginId: PluginId)
}
