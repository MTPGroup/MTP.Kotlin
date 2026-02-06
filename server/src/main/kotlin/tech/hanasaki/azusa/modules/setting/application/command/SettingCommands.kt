package tech.hanasaki.azusa.modules.setting.application.command

import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId

data class GetSettingCommand(
    val uid: UserId,
)

data class UpdateSettingCommand(
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfig>,
    val activeThemeId: ThemeId?,
    val activeLLMConfigId: LLMConfigId?,
)
