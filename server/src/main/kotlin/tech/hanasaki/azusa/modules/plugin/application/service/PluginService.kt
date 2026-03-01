package tech.hanasaki.azusa.modules.plugin.application.service

import tech.hanasaki.azusa.modules.plugin.application.port.`in`.PluginUseCasePort
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginLikeRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort


class PluginService(
    private val pluginRepository: PluginRepositoryPort,
    private val likeRepository: PluginLikeRepositoryPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : PluginUseCasePort {

    override suspend fun listApprovedPlugins(page: Int, limit: Int): PageResult<Plugin> = tx.readOnly {
        pluginRepository.findApprovedPaged(page, limit)
    }

    override suspend fun searchPlugins(query: String, page: Int, limit: Int): PageResult<Plugin> = tx.readOnly {
        pluginRepository.searchApproved(query, page, limit)
    }

    override suspend fun listPendingPlugins(page: Int, limit: Int): PageResult<Plugin> = tx.readOnly {
        pluginRepository.findByStatusPaged(PluginStatus.PENDING, page, limit)
    }

    override suspend fun listMyPlugins(userId: UserId, page: Int, limit: Int): PageResult<Plugin> = tx.readOnly {
        pluginRepository.findByAuthorIdPaged(userId, page, limit)
    }

    override suspend fun getPlugin(pluginId: PluginId): Plugin = tx.readOnly {
        pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
    }

    override suspend fun getApprovedPlugin(pluginId: PluginId): Plugin = tx.readOnly {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.status != PluginStatus.APPROVED) {
            throw NotFoundException("Plugin not found")
        }
        plugin
    }

    override suspend fun createPlugin(
        authorId: UserId,
        name: String,
        description: String,
        version: String,
        schema: PluginSchema,
        executionConfig: PluginExecutionConfig,
    ): Plugin = tx.execute {
        val plugin = Plugin.create(
            name = name,
            description = description,
            version = version,
            schema = schema,
            executionConfig = executionConfig,
            authorId = authorId,
        )
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        plugin
    }

    override suspend fun updatePlugin(
        userId: UserId,
        pluginId: PluginId,
        name: String,
        description: String,
        version: String,
        schema: PluginSchema,
        executionConfig: PluginExecutionConfig,
    ): Plugin = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        plugin.update(
            name = name,
            description = description,
            version = version,
            schema = schema,
            executionConfig = executionConfig,
        )
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        plugin
    }

    override suspend fun deletePlugin(userId: UserId, pluginId: PluginId) = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.authorId != userId) {
            throw AuthorizationException("Access denied")
        }
        pluginRepository.deleteById(pluginId)
    }

    override suspend fun approvePlugin(pluginId: PluginId): Plugin = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.status != PluginStatus.PENDING) {
            throw ConflictException("Only pending plugins can be approved")
        }
        plugin.approve()
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        plugin
    }

    override suspend fun rejectPlugin(pluginId: PluginId): Plugin = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (plugin.status != PluginStatus.PENDING) {
            throw ConflictException("Only pending plugins can be rejected")
        }
        plugin.reject()
        pluginRepository.save(plugin)
        plugin.publishAndClear(domainEventBus)
        plugin
    }

    override suspend fun likePlugin(userId: UserId, pluginId: PluginId) = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (likeRepository.exists(userId, pluginId)) {
            throw ConflictException("Already liked")
        }
        likeRepository.like(userId, pluginId)
        plugin.incrementLikeCount()
        pluginRepository.save(plugin)
    }

    override suspend fun unlikePlugin(userId: UserId, pluginId: PluginId) = tx.execute {
        val plugin = pluginRepository.findById(pluginId)
            ?: throw NotFoundException("Plugin not found")
        if (!likeRepository.exists(userId, pluginId)) {
            throw NotFoundException("Like not found")
        }
        likeRepository.unlike(userId, pluginId)
        plugin.decrementLikeCount()
        pluginRepository.save(plugin)
    }
}
