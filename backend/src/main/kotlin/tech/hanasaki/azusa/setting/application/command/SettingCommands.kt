package tech.hanasaki.azusa.setting.application.command

import tech.hanasaki.azusa.setting.domain.model.AppTheme
import tech.hanasaki.azusa.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.common.UserId

data class GetSettingCommand(
    val uid: UserId,
)

data class UpdateSettingCommand(
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfig>,
    val activeThemeId: ThemeId?,
    val activeLLMConfigId: LLMConfigId?,
)
