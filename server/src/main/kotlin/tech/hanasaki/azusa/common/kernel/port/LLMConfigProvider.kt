package tech.hanasaki.azusa.common.kernel.port

import tech.hanasaki.azusa.common.kernel.model.LLMConfig
import tech.hanasaki.azusa.common.kernel.model.UserId

interface LLMConfigProvider {
    suspend fun getActiveConfig(userId: UserId): LLMConfig?
}