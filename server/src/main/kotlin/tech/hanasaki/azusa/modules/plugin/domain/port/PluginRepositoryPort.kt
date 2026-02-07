package tech.hanasaki.azusa.modules.plugin.domain.port

import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus

interface PluginRepositoryPort {
    suspend fun findById(id: PluginId): Plugin?
    suspend fun save(plugin: Plugin)
    suspend fun deleteById(id: PluginId)
    suspend fun findApprovedPaged(page: Int, limit: Int): PageResult<Plugin>
    suspend fun searchApproved(query: String, page: Int, limit: Int): PageResult<Plugin>
    suspend fun findByStatusPaged(status: PluginStatus, page: Int, limit: Int): PageResult<Plugin>
    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<Plugin>
}
