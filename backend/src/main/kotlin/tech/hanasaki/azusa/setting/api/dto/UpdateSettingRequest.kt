package tech.hanasaki.azusa.setting.api.dto

import tech.hanasaki.azusa.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.setting.domain.model.AppTheme
import tech.hanasaki.azusa.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.shared.ThemeId

data class UpdateSettingRequest(
    val theme: AppTheme,
    val llmConfigs: Set<CreateLLMConfigRequest>,
    val activeThemeId: ThemeId?,
    val activeLlmConfigId: LLMConfigId?,
) {
    fun toCommand(): UpdateSettingCommand = UpdateSettingCommand(
        theme = theme,
        llmConfigs = llmConfigs.map { it.toDomain() }.toSet(),
        activeThemeId = activeThemeId,
        activeLLMConfigId = activeLlmConfigId
    )
}
