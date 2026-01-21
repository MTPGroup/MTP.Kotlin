package tech.hanasaki.azusa.modules.setting.application.command

import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.shared.domain.model.ThemeId
import tech.hanasaki.azusa.shared.domain.model.UserId

data class GetSettingCommand(
    val uid: UserId,
)

data class UpdateSettingCommand(
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfig>,
    val activeThemeId: ThemeId?,
    val activeLLMConfigId: LLMConfigId?,
)
