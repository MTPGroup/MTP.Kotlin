package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table.PluginTable
import tech.hanasaki.azusa.common.domain.model.PluginId
import tech.hanasaki.azusa.common.domain.model.UserId

object PluginMapper {
    fun toDomain(row: ResultRow): Plugin = Plugin.reconstitute(
        id = PluginId(row[PluginTable.id]),
        name = row[PluginTable.name],
        description = row[PluginTable.description],
        version = row[PluginTable.version],
        schema = row[PluginTable.schema],
        code = row[PluginTable.code],
        authorId = UserId(row[PluginTable.authorId]),
        status = row[PluginTable.status],
        likeCount = row[PluginTable.liked],
        createdAt = row[PluginTable.createdAt],
        updatedAt = row[PluginTable.updatedAt],
    )

    fun toEntity(domain: Plugin, target: UpdateBuilder<*>) {
        target[PluginTable.name] = domain.name
        target[PluginTable.description] = domain.description
        target[PluginTable.version] = domain.version
        target[PluginTable.schema] = domain.schema
        target[PluginTable.code] = domain.code
        target[PluginTable.authorId] = domain.authorId.value
        target[PluginTable.status] = domain.status
        target[PluginTable.liked] = domain.likeCount
    }
}