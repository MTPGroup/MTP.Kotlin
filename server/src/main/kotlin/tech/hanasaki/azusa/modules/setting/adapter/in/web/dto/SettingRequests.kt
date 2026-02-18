package tech.hanasaki.azusa.modules.setting.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfigId
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import kotlin.uuid.Uuid

@Serializable
data class LLMConfigRequest(
    val id: Uuid? = null,
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    fun toDomain(): LLMConfig = LLMConfig(
        id = LLMConfigId(id ?: Uuid.random()),
        provider = provider,
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        temperature = temperature,
        maxTokens = maxTokens,
        runOnClient = runOnClient,
    )
}

@Serializable
data class UpdateSettingRequest(
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfigRequest>,
    val activeThemeId: Uuid?,
    val activeLlmConfigId: Uuid?,
) {
    val activeThemeIdVo: ThemeId? get() = activeThemeId?.let { ThemeId(it) }
    val activeLlmConfigIdVo: LLMConfigId? get() = activeLlmConfigId?.let { LLMConfigId(it) }
}
