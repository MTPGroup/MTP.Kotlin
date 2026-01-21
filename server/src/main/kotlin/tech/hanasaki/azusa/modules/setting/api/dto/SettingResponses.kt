package tech.hanasaki.azusa.modules.setting.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import java.util.UUID

@Serializable
data class LLMConfigResponse(
    @Contextual val id: UUID,
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int?,
    val runOnClient: Boolean,
)

@Serializable
data class SettingResponse(
    @Contextual val uid: UUID,
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfigResponse>,
    @Contextual val activeThemeId: UUID?,
    @Contextual val activeLlmConfigId: UUID?,
    val createdAt: String,
    val updatedAt: String,
)

fun LLMConfig.toResponse(): LLMConfigResponse = LLMConfigResponse(
    id = id.value,
    provider = provider,
    baseUrl = baseUrl,
    apiKey = apiKey,
    model = model,
    temperature = temperature,
    maxTokens = maxTokens,
    runOnClient = runOnClient,
)

fun Setting.toResponse(): SettingResponse = SettingResponse(
    uid = uid.value,
    theme = theme,
    llmConfigs = llmConfigs.map { it.toResponse() }.toSet(),
    activeThemeId = activeThemeId?.value,
    activeLlmConfigId = activeLlmConfigId?.value,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
