package tech.hanasaki.azusa.modules.setting.application.service

import tech.hanasaki.azusa.modules.setting.application.port.`in`.SettingUseCasePort
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.port.SettingRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfigId
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class SettingService(
    private val settingRepository: SettingRepositoryPort,
    private val tx: TransactionalPort,
) : SettingUseCasePort {
    override suspend fun createSetting(userId: UserId) = tx.execute {
        val setting = Setting.create(userId)
        settingRepository.save(setting)
    }

    override suspend fun getSetting(userId: UserId): Setting = tx.readOnly {
        settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
    }

    override suspend fun updateSetting(
        userId: UserId,
        theme: AppTheme,
        llmConfigs: Set<LLMConfig>,
        activeThemeId: ThemeId?,
        activeLlmConfigId: LLMConfigId?,
    ): Setting = tx.execute {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting
            .changeTheme(theme)
            .applyTheme(activeThemeId)
            .replaceLlmConfigs(llmConfigs, activeLlmConfigId)
        settingRepository.save(updatedSetting)
        updatedSetting
    }
}
