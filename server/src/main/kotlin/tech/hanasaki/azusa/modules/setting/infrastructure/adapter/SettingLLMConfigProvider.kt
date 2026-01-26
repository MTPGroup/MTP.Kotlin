package tech.hanasaki.azusa.modules.setting.infrastructure.adapter

import tech.hanasaki.azusa.common.kernel.model.LLMConfig
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.common.kernel.port.LLMConfigProvider
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository

class SettingLLMConfigProvider(
    private val settingRepository: SettingRepository,
) : LLMConfigProvider {
    override suspend fun getActiveConfig(userId: UserId): LLMConfig? {
        val setting = settingRepository.findByUserId(userId)
        return setting?.getActiveLlmConfig()
    }
}