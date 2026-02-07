package tech.hanasaki.azusa.modules.setting.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table


object LlmConfigsTable : Table("llm_configs") {
    val id = uuid("id")
    val settingId = uuid("setting_id").references(SettingsTable.id, onDelete = ReferenceOption.CASCADE)
    val provider = varchar("provider", 32)
    val baseUrl = varchar("base_url", 255)
    val apiKey = varchar("api_key", 255)
    val model = varchar("model", 64)
    val temperature = float("temperature")
    val maxTokens = integer("max_tokens").nullable()
    val runOnClient = bool("run_on_client")

    override val primaryKey = PrimaryKey(id)
}
