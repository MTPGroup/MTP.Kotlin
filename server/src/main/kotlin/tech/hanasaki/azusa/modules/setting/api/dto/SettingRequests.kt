package tech.hanasaki.azusa.modules.setting.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.modules.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import kotlin.uuid.Uuid


@Serializable
data class CreateLLMConfigRequest(
    @Contextual val id: Uuid? = null,
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
data class UpdateLLMConfigRequest(
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    fun toDomain(configId: Uuid): LLMConfig = LLMConfig(
        id = LLMConfigId(configId),
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
    val llmConfigs: Set<CreateLLMConfigRequest>,
    @Contextual val activeThemeId: Uuid?,
    @Contextual val activeLlmConfigId: Uuid?,
) {
    fun toCommand(): UpdateSettingCommand = UpdateSettingCommand(
        theme = theme,
        llmConfigs = llmConfigs.map { it.toDomain() }.toSet(),
        activeThemeId = activeThemeId?.let { ThemeId(it) },
        activeLLMConfigId = activeLlmConfigId?.let { LLMConfigId(it) },
    )
}
