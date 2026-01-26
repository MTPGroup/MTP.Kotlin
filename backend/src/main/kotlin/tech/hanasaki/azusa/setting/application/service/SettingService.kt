package tech.hanasaki.azusa.setting.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hanasaki.azusa.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.common.ConflictException
import tech.hanasaki.azusa.common.NotFoundException
import tech.hanasaki.azusa.common.UserId

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
            .replaceLlmConfigs(cmd.llmConfigs, cmd.activeLLMConfigId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    @Transactional
    fun listLlmConfigs(userId: UserId): Set<LLMConfig> {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        return currentSetting.llmConfigs
    }

    @Transactional
    fun getLlmConfig(userId: UserId, configId: LLMConfigId): LLMConfig {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        return currentSetting.llmConfigs.find { it.id == configId }
            ?: throw NotFoundException("LLM config not found")
    }

    @Transactional
    fun addLlmConfig(userId: UserId, config: LLMConfig): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting.saveLlmConfig(config)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    @Transactional
    fun updateLlmConfig(userId: UserId, config: LLMConfig): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        if (currentSetting.llmConfigs.none { it.id == config.id }) {
            throw NotFoundException("LLM config not found")
        }
        val updatedSetting = currentSetting.saveLlmConfig(config)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    @Transactional
    fun deleteLlmConfig(userId: UserId, configId: LLMConfigId): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        if (currentSetting.activeLlmConfigId == configId) {
            throw ConflictException("Cannot remove active LLM config")
        }
        val updatedSetting = currentSetting.removeLlmConfig(configId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    @Transactional
    fun selectLlmConfig(userId: UserId, configId: LLMConfigId): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting.selectLlmConfig(configId)
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
