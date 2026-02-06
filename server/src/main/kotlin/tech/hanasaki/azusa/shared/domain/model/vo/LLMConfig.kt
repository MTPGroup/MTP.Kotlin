package tech.hanasaki.azusa.shared.domain.model.vo

import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import kotlin.uuid.Uuid

data class LLMConfig(
    val id: LLMConfigId = LLMConfigId(Uuid.Companion.random()),
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
)