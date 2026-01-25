package tech.hanasaki.azusa.shared.domain.port

import tech.hanasaki.azusa.shared.domain.model.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.UserId

interface LLMConfigProvider {
    suspend fun getActiveConfig(userId: UserId): LLMConfig?
}