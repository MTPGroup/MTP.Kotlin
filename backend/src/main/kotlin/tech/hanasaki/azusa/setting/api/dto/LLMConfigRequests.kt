package tech.hanasaki.azusa.setting.api.dto

import tech.hanasaki.azusa.setting.domain.model.LLMConfig
import tech.hanasaki.azusa.setting.domain.model.LLMConfigId
import tech.hanasaki.azusa.setting.domain.model.LLMProvider
import java.util.UUID

data class CreateLLMConfigRequest(
    val id: LLMConfigId? = null,
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    fun toDomain(): LLMConfig = LLMConfig(
        id = id ?: LLMConfigId(UUID.randomUUID()),
        provider = provider,
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        temperature = temperature,
        maxTokens = maxTokens,
        runOnClient = runOnClient
    )
}

data class UpdateLLMConfigRequest(
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    fun toDomain(configId: LLMConfigId): LLMConfig = LLMConfig(
        id = configId,
        provider = provider,
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        temperature = temperature,
        maxTokens = maxTokens,
        runOnClient = runOnClient
    )
}
