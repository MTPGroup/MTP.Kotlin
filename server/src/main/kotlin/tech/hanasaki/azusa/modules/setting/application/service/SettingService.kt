package tech.hanasaki.azusa.modules.setting.application.service

import tech.hanasaki.azusa.shared.domain.exception.ConflictException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.modules.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository

class SettingService(
    private val settingRepository: SettingRepository,
) {
    suspend fun getSetting(cmd: GetSettingCommand): Setting {
        return settingRepository.findByUserId(cmd.uid)
            ?: throw NotFoundException("Setting not found")
    }

    suspend fun updateSetting(userId: UserId, cmd: UpdateSettingCommand): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting
            .changeTheme(cmd.theme)
            .applyTheme(cmd.activeThemeId)
            .replaceLlmConfigs(cmd.llmConfigs, cmd.activeLLMConfigId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    suspend fun listLlmConfigs(userId: UserId): Set<LLMConfig> {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        return currentSetting.llmConfigs
    }

    suspend fun getLlmConfig(userId: UserId, configId: LLMConfigId): LLMConfig {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        return currentSetting.llmConfigs.find { it.id == configId }
            ?: throw NotFoundException("LLM config not found")
    }

    suspend fun addLlmConfig(userId: UserId, config: LLMConfig): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting.saveLlmConfig(config)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    suspend fun updateLlmConfig(userId: UserId, config: LLMConfig): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        if (currentSetting.llmConfigs.none { it.id == config.id }) {
            throw NotFoundException("LLM config not found")
        }
        val updatedSetting = currentSetting.saveLlmConfig(config)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    suspend fun deleteLlmConfig(userId: UserId, configId: LLMConfigId): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        if (currentSetting.activeLlmConfigId == configId) {
            throw ConflictException("Cannot remove active LLM config")
        }
        val updatedSetting = currentSetting.removeLlmConfig(configId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }

    suspend fun selectLlmConfig(userId: UserId, configId: LLMConfigId): Setting {
        val currentSetting = settingRepository.findByUserId(userId)
            ?: throw NotFoundException("Setting not found")
        val updatedSetting = currentSetting.selectLlmConfig(configId)
        settingRepository.save(updatedSetting)
        return updatedSetting
    }
}
