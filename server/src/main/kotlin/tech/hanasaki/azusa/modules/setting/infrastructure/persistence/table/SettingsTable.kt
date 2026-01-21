package tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object SettingsTable : Table("settings") {
    val id = uuid("uid")
    val theme = varchar("theme", 30)
    val activeThemeId = uuid("active_theme_id").nullable()
    val activeLlmConfigId = uuid("active_llm_config_id").nullable()
    val createdAt = datetime("created_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))
    val updatedAt = datetime("updated_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))

    override val primaryKey = PrimaryKey(id)
}
