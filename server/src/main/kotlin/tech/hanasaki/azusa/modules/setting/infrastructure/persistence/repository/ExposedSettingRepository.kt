package tech.hanasaki.azusa.modules.setting.infrastructure.persistence.repository

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.setting.domain.model.*
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.LlmConfigsTable
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.SettingsTable
import tech.hanasaki.azusa.shared.domain.model.ThemeId
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedSettingRepository : SettingRepository {
    override suspend fun findByUserId(userId: UserId): Setting? = dbQuery {
        val settingRow = SettingsTable.selectAll()
            .where { SettingsTable.id eq userId.value }
            .singleOrNull() ?: return@dbQuery null
        val llmRows = LlmConfigsTable.selectAll()
            .where { LlmConfigsTable.settingId eq userId.value }.toList()
        toDomain(settingRow, llmRows)
    }

    override suspend fun save(setting: Setting): Unit = dbQuery {
        val updatedRows = SettingsTable.update({ SettingsTable.id eq setting.uid.value }) {
            it[theme] = setting.theme.name
            it[activeThemeId] = setting.activeThemeId?.value
            it[activeLlmConfigId] = setting.activeLlmConfigId?.value
            it[updatedAt] = setting.updatedAt.toLocalDateTime(TimeZone.UTC)
        }
        if (updatedRows == 0) {
            SettingsTable.insert {
                it[id] = setting.uid.value
                it[theme] = setting.theme.name
                it[activeThemeId] = setting.activeThemeId?.value
                it[activeLlmConfigId] = setting.activeLlmConfigId?.value
                it[createdAt] = setting.createdAt.toLocalDateTime(TimeZone.UTC)
                it[updatedAt] = setting.updatedAt.toLocalDateTime(TimeZone.UTC)
            }
        }

        LlmConfigsTable.deleteWhere { LlmConfigsTable.settingId eq setting.uid.value }
        setting.llmConfigs.forEach { config ->
            LlmConfigsTable.insert {
                it[id] = config.id.value
                it[settingId] = setting.uid.value
                it[provider] = config.provider.name
                it[baseUrl] = config.baseUrl
                it[apiKey] = config.apiKey
                it[model] = config.model
                it[temperature] = config.temperature
                it[maxTokens] = config.maxTokens
                it[runOnClient] = config.runOnClient
            }
        }
    }

    private fun toDomain(settingRow: ResultRow, llmRows: List<ResultRow>): Setting {
        val themeValue = settingRow[SettingsTable.theme]
        val theme = runCatching { AppTheme.valueOf(themeValue.uppercase()) }.getOrElse { AppTheme.SYSTEM }
        val llmConfigs = llmRows.map { row ->
            LLMConfig(
                id = LLMConfigId(row[LlmConfigsTable.id]),
                provider = LLMProvider.valueOf(row[LlmConfigsTable.provider].uppercase()),
                baseUrl = row[LlmConfigsTable.baseUrl],
                apiKey = row[LlmConfigsTable.apiKey],
                model = row[LlmConfigsTable.model],
                temperature = row[LlmConfigsTable.temperature],
                maxTokens = row[LlmConfigsTable.maxTokens],
                runOnClient = row[LlmConfigsTable.runOnClient],
            )
        }.toSet()

        return Setting(
            uid = UserId(settingRow[SettingsTable.id]),
            theme = theme,
            llmConfigs = llmConfigs,
            activeThemeId = settingRow[SettingsTable.activeThemeId]?.let { ThemeId(it) },
            activeLlmConfigId = settingRow[SettingsTable.activeLlmConfigId]?.let { LLMConfigId(it) },
            createdAt = settingRow[SettingsTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = settingRow[SettingsTable.updatedAt].toInstant(TimeZone.UTC),
        )
    }
}
