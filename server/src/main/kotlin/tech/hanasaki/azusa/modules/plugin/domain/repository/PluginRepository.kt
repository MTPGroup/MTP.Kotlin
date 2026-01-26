package tech.hanasaki.azusa.modules.plugin.domain.repository

import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import tech.hanasaki.azusa.common.kernel.model.PageResult
import tech.hanasaki.azusa.common.kernel.model.PluginId
import tech.hanasaki.azusa.common.kernel.model.UserId

interface PluginRepository {
    suspend fun findById(id: PluginId): Plugin?
    suspend fun save(plugin: Plugin)
    suspend fun deleteById(id: PluginId)

    /**
     * 查询已通过审核的插件（分页）
     */
    suspend fun findApprovedPaged(page: Int, limit: Int): PageResult<Plugin>

    /**
     * 搜索已通过审核的插件（按名称或描述模糊匹配）
     */
    suspend fun searchApproved(query: String, page: Int, limit: Int): PageResult<Plugin>

    /**
     * 按状态查询插件（分页）
     */
    suspend fun findByStatusPaged(status: PluginStatus, page: Int, limit: Int): PageResult<Plugin>

    /**
     * 查询用户创建的插件
     */
    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<Plugin>
}
