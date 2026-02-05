package tech.hanasaki.azusa.modules.theme.infrastructure.persistence.repository

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.ThemeId
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.theme.domain.model.Theme
import tech.hanasaki.azusa.modules.theme.domain.repository.ThemeRepository
import tech.hanasaki.azusa.modules.theme.infrastructure.persistence.table.ThemeTable

class ExposedThemeRepository : ThemeRepository {
    override suspend fun findByThemeId(id: ThemeId): Theme? = dbQuery {
        ThemeTable.selectAll()
            .where { ThemeTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun findByAuthorId(authorId: UserId): List<Theme> = dbQuery {
        ThemeTable.selectAll()
            .where { ThemeTable.authorId eq authorId.value }
            .map(::toDomain)
    }

    override suspend fun save(theme: Theme): Unit = dbQuery {
        val updatedRows = ThemeTable.update({ ThemeTable.id eq theme.id.value }) {
            it[authorId] = theme.authorId.value
            it[name] = theme.name
            it[description] = theme.description
            it[previewUrl] = theme.previewUrl
            it[data] = theme.data
            it[downloadCount] = theme.downloadCount
            it[version] = theme.version
            it[updatedAt] = theme.updatedAt.toLocalDateTime(TimeZone.UTC)
        }
        if (updatedRows == 0) {
            ThemeTable.insert {
                it[id] = theme.id.value
                it[authorId] = theme.authorId.value
                it[name] = theme.name
                it[description] = theme.description
                it[previewUrl] = theme.previewUrl
                it[data] = theme.data
                it[downloadCount] = theme.downloadCount
                it[version] = theme.version
                it[createdAt] = theme.createdAt.toLocalDateTime(TimeZone.UTC)
                it[updatedAt] = theme.updatedAt.toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    override suspend fun deleteByThemeId(id: ThemeId): Unit = dbQuery {
        ThemeTable.deleteWhere { ThemeTable.id eq id.value }
    }

    private fun toDomain(row: ResultRow): Theme = Theme(
        id = ThemeId(row[ThemeTable.id]),
        authorId = UserId(row[ThemeTable.authorId]),
        name = row[ThemeTable.name],
        description = row[ThemeTable.description],
        previewUrl = row[ThemeTable.previewUrl],
        data = row[ThemeTable.data],
        downloadCount = row[ThemeTable.downloadCount],
        version = row[ThemeTable.version],
        createdAt = row[ThemeTable.createdAt].toInstant(TimeZone.UTC),
        updatedAt = row[ThemeTable.updatedAt].toInstant(TimeZone.UTC),
    )
}
