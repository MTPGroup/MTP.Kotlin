package tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object SettingsTable : Table("settings") {
    val id = uuid("uid")
    val theme = varchar("theme", 30)
    val activeThemeId = uuid("active_theme_id").nullable()
    val activeLlmConfigId = uuid("active_llm_config_id").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
