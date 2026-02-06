package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginRepository
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.mapper.PluginMapper
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table.PluginTable

class ExposedPluginRepository : PluginRepository {
    override suspend fun findById(id: PluginId): Plugin? =
        PluginTable.selectAll()
            .where { PluginTable.id eq id.value }
            .map(PluginMapper::toDomain)
            .singleOrNull()

    override suspend fun save(plugin: Plugin) {
        val updatedRows = PluginTable.update({ PluginTable.id eq plugin.id.value }) {
            PluginMapper.toEntity(plugin, it)
            it[updatedAt] = plugin.updatedAt
        }
        if (updatedRows == 0) {
            PluginTable.insert {
                it[id] = plugin.id.value
                PluginMapper.toEntity(plugin, it)
                it[createdAt] = plugin.createdAt
                it[updatedAt] = plugin.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: PluginId) {
        PluginTable.deleteWhere { PluginTable.id eq id.value }
    }

    override suspend fun findApprovedPaged(page: Int, limit: Int): PageResult<Plugin> {
        val condition = { PluginTable.status eq PluginStatus.APPROVED }

        val total = getTotalCount(condition)

        val items = PluginTable.selectAll()
            .where(condition)
            .orderBy(PluginTable.liked, SortOrder.DESC)
            .orderBy(PluginTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(PluginMapper::toDomain)

        return PageResult(items, total, page, limit)
    }

    override suspend fun searchApproved(query: String, page: Int, limit: Int): PageResult<Plugin> {
        val searchPattern = "%${query.lowercase()}%"
        val condition = {
            (PluginTable.status eq PluginStatus.APPROVED) and
                    ((PluginTable.name.lowerCase() like searchPattern) or
                            (PluginTable.description.lowerCase() like searchPattern))
        }

        val total = getTotalCount(condition)

        val items = PluginTable.selectAll()
            .where(condition)
            .orderBy(PluginTable.liked, SortOrder.DESC)
            .orderBy(PluginTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(PluginMapper::toDomain)

        return PageResult(items, total, page, limit)
    }

    override suspend fun findByStatusPaged(status: PluginStatus, page: Int, limit: Int): PageResult<Plugin> {
        val condition = { PluginTable.status eq status }

        val total = getTotalCount(condition)

        val items = PluginTable.selectAll()
            .where(condition)
            .orderBy(PluginTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(PluginMapper::toDomain)

        return PageResult(items, total, page, limit)
    }

    override suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<Plugin> {
        val condition = { PluginTable.authorId eq authorId.value }

        val total = getTotalCount(condition)

        val items = PluginTable.selectAll()
            .where(condition)
            .orderBy(PluginTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(PluginMapper::toDomain)

        return PageResult(items, total, page, limit)
    }

    private fun getTotalCount(condition: () -> Op<Boolean>): Long =
        PluginTable.selectAll()
            .where(condition)
            .count()
}
