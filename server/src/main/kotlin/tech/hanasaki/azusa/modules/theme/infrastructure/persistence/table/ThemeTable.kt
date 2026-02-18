package tech.hanasaki.azusa.modules.theme.infrastructure.persistence.table

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import tech.hanasaki.azusa.modules.theme.domain.model.ThemeDefinition

val themeJsonFormat = Json { prettyPrint = true }


object ThemeTable : Table("themes") {
    val id = uuid("id")
    val authorId = uuid("author_id")
    val name = varchar("name", 64)
    val description = varchar("description", 512).nullable()
    val previewUrl = varchar("preview_url", 1024).nullable()
    val data = jsonb<ThemeDefinition>("data", themeJsonFormat)
    val downloadCount = integer("download_count")
    val version = varchar("version", 32)
    val createdAt = datetime("created_at").default(kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC))
    val updatedAt = datetime("updated_at").default(kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC))

    override val primaryKey = PrimaryKey(id)
}
