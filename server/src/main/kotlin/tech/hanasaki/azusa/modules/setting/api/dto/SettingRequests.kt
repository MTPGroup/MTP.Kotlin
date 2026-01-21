package tech.hanasaki.azusa.modules.setting.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.setting.application.command.UpdateSettingCommand
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.modules.setting.domain.model.LLMProvider
import tech.hanasaki.azusa.modules.setting.domain.model.AppTheme
import tech.hanasaki.azusa.shared.domain.model.ThemeId
import java.util.UUID

@Serializable
data class CreateLLMConfigRequest(
    @Contextual val id: UUID? = null,
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    fun toDomain(): LLMConfig = LLMConfig(
        id = LLMConfigId(id ?: UUID.randomUUID()),
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
    fun toDomain(configId: UUID): LLMConfig = LLMConfig(
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
    @Contextual val activeThemeId: UUID?,
    @Contextual val activeLlmConfigId: UUID?,
) {
    fun toCommand(): UpdateSettingCommand = UpdateSettingCommand(
        theme = theme,
        llmConfigs = llmConfigs.map { it.toDomain() }.toSet(),
        activeThemeId = activeThemeId?.let { ThemeId(it) },
        activeLLMConfigId = activeLlmConfigId?.let { LLMConfigId(it) },
    )
}
