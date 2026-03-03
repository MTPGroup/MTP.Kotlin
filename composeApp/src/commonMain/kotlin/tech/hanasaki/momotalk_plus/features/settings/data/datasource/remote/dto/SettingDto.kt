package tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class RemoteAppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

@Serializable
enum class RemoteLLMProvider {
    OPENAI,
    ALIBABA,
    DEEPSEEK,
    GOOGLE,
    ANTHROPIC,
    OLLAMA,
    OFFICIAL,
}

@Serializable
data class RemoteLLMConfig(
    val id: String,
    val provider: RemoteLLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
)

@Serializable
data class SettingResponseData(
    val uid: String,
    val theme: RemoteAppTheme,
    val llmConfigs: Set<RemoteLLMConfig>,
    val activeThemeId: String? = null,
    val activeLlmConfigId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class UpdateSettingRequest(
    val theme: RemoteAppTheme,
    val llmConfigs: Set<RemoteLLMConfig>,
    val activeThemeId: String? = null,
    val activeLlmConfigId: String? = null,
)
