package tech.hanasaki.azusa.modules.setting.infrastructure.adapter

import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.shared.domain.model.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.domain.port.LLMConfigProvider

class SettingLLMConfigProvider(
    private val settingRepository: SettingRepository,
) : LLMConfigProvider {
    override suspend fun getActiveConfig(userId: UserId): LLMConfig? {
        val setting = settingRepository.findByUserId(userId)
        return setting?.getActiveLlmConfig()
    }
}