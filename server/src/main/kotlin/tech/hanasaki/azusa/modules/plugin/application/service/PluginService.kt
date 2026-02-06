package tech.hanasaki.azusa.modules.plugin.application.service

import tech.hanasaki.azusa.modules.plugin.application.command.CreatePluginCommand
import tech.hanasaki.azusa.modules.plugin.application.command.UpdatePluginCommand
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginLikeRepository
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort


class PluginService(
    private val pluginRepository: PluginRepository,
    private val likeRepository: PluginLikeRepository,
    private val domainEventBus: DomainEventBusPort,
) {

    // ===插件===

    /**
     * 获取已通过审核的插件列表（分页）
     */
    suspend fun listApprovedPlugins(page: Int, limit: Int): PageResult<Plugin> {
        return pluginRepository.findApprovedPaged(page, limit)
    }

    /**
     * 搜索已通过审核的插件
     */
    suspend fun searchPlugins(query: String, page: Int, limit: Int): PageResult<Plugin> {
        return pluginRepository.searchApproved(query, page, limit)
    }

    /**
     * 获取待审核的插件（管理员）
     */
    suspend fun listPendingPlugins(page: Int, limit: Int): PageResult<Plugin> {
        return pluginRepository.findByStatusPaged(PluginStatus.PENDING, page, limit)
    }

    /**
     * 获取用户创建的插件
     */
    suspend fun listMyPlugins(userId: UserId, page: Int, limit: Int): PageResult<Plugin> {
        return pluginRepository.findByAuthorIdPaged(userId, page, limit)
    }

    /**
     * 获取插件详情
     */
    suspend fun getPlugin(pluginId: PluginId): Plugin {
        return pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
    }

    /**
     * 创建插件（状态为 PENDING）
     */
    suspend fun createPlugin(authorId: UserId, cmd: CreatePluginCommand): Plugin {
        val plugin = Plugin.create(
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            schema = cmd.schema,
            code = cmd.code,
            authorId = authorId,
        )
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        return plugin
    }

    /**
     * 更新插件（仅作者可操作）
     */
    suspend fun updatePlugin(userId: UserId, pluginId: PluginId, cmd: UpdatePluginCommand): Plugin {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        plugin.update(
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            schema = cmd.schema,
            code = cmd.code,
        )
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        return plugin
    }

    /**
     * 删除插件（仅作者可操作）
     */
    suspend fun deletePlugin(userId: UserId, pluginId: PluginId) {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        pluginRepository.deleteById(pluginId)
    }

    // ===审核操作（管理员）===

    /**
     * 审批通过
     */
    suspend fun approvePlugin(pluginId: PluginId): Plugin {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        plugin.approve()
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        return plugin
    }

    /**
     * 审批拒绝
     */
    suspend fun rejectPlugin(pluginId: PluginId): Plugin {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        plugin.reject()
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        return plugin
    }

    // ===点赞管理===

    /**
     * 点赞插件
     */
    suspend fun likePlugin(userId: UserId, pluginId: PluginId) {
        pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (likeRepository.exists(userId, pluginId)) {
            throw ConflictException("Already liked")
        }
        likeRepository.like(userId, pluginId)
    }

    /**
     * 取消点赞
     */
    suspend fun unlikePlugin(userId: UserId, pluginId: PluginId) {
        if (!likeRepository.exists(userId, pluginId)) {
            throw NotFoundException("Like not found")
        }
        likeRepository.unlike(userId, pluginId)
    }
}
