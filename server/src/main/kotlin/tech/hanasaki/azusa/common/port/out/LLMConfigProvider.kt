package tech.hanasaki.azusa.common.port.out

import tech.hanasaki.azusa.common.domain.model.LLMConfig
import tech.hanasaki.azusa.common.domain.model.UserId

interface LLMConfigProvider {
    suspend fun getActiveConfig(userId: UserId): LLMConfig?
}