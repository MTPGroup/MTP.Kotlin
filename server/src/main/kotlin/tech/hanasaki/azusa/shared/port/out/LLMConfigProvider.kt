package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface LLMConfigProvider {
    suspend fun getActiveConfig(userId: UserId): LLMConfig?
}