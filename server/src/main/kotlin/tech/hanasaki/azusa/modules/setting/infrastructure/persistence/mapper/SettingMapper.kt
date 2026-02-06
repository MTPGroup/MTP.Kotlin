package tech.hanasaki.azusa.modules.setting.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.LlmConfigsTable
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.table.SettingsTable

object SettingMapper {
    fun toDomain(settingRow: ResultRow, llmRows: List<ResultRow>): Setting {
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
            createdAt = settingRow[SettingsTable.createdAt],
            updatedAt = settingRow[SettingsTable.updatedAt],
        )
    }

    fun toEntity(domain: Setting, target: UpdateBuilder<*>) {
        target[SettingsTable.theme] = domain.theme.name
        target[SettingsTable.activeThemeId] = domain.activeThemeId?.value
        target[SettingsTable.activeLlmConfigId] = domain.activeLlmConfigId?.value
    }
}