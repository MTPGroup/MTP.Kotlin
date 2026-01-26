package tech.hanasaki.azusa.common.kernel.model

import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import java.util.*

data class LLMConfig(
    val id: LLMConfigId = LLMConfigId(UUID.randomUUID()),
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
)
