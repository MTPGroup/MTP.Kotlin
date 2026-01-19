package tech.hanasaki.azusa.setting.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hanasaki.azusa.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.shared.NotFoundException
import tech.hanasaki.azusa.shared.UserId

@Service
class SettingService(
    private val settingRepository: SettingRepository,
) {
    @Transactional
    fun getSetting(cmd: GetSettingCommand): Setting {
        return settingRepository.findByUserId(cmd.uid) ?: run {
            val setting = Setting.init(cmd.uid)
            settingRepository.save(setting)
            setting
        }
    }

    @Transactional
    fun updateSetting(userId: UserId, cmd: UpdateSettingCommand): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting
            .changeTheme(cmd.theme)
            .applyTheme(cmd.activeThemeId)
            .selectLlmConfig(cmd.activeLLMConfigId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    @Transactional
    fun createSetting(userId: UserId) {
        if (settingRepository.findByUserId(userId) == null) {
            val setting = Setting.init(userId)
            settingRepository.save(setting)
        }
    }
}