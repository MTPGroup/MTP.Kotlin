package tech.hanasaki.azusa.modules.setting.application.port.`in`

import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfigId
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface SettingUseCasePort {
    suspend fun createSetting(userId: UserId)
    suspend fun getSetting(userId: UserId): Setting
    suspend fun updateSetting(
        userId: UserId,
        theme: AppTheme,
        llmConfigs: Set<LLMConfig>,
        activeThemeId: ThemeId?,
        activeLlmConfigId: LLMConfigId?,
    ): Setting
}
