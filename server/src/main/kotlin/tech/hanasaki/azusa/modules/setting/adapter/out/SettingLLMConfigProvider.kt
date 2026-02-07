package tech.hanasaki.azusa.modules.setting.adapter.out

import tech.hanasaki.azusa.modules.setting.domain.port.SettingRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.LLMConfigProvider

class SettingLLMConfigProvider(
    private val settingRepository: SettingRepositoryPort,
) : LLMConfigProvider {
    override suspend fun getActiveConfig(userId: UserId): LLMConfig? {
        val setting = settingRepository.findByUserId(userId)
        return setting?.getActiveLlmConfig()
    }
}