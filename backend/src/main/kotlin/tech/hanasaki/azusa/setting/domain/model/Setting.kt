package tech.hanasaki.azusa.setting.domain.model

import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.common.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@JvmInline
value class LLMConfigId(val value: UUID)

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

enum class LLMProvider {
    OPENAI, // 标准 OpenAI 协议
    AZURE,  // Azure OpenAI
    CUSTOM  // 自建/第三方兼容接口 (如 Ollama, DeepSeek)
}

data class LLMConfig(
    val id: LLMConfigId = LLMConfigId(UUID.randomUUID()),
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
) {
    companion object {
        val DEFAULT = LLMConfig(
            provider = LLMProvider.OPENAI,
            baseUrl = "https://api.openai.com/v1",
            apiKey = "",
            model = "gpt-3.5-turbo",
            temperature = 0.7f,
        )
    }
}

data class Setting(
    val uid: UserId,
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfig>,
    val activeThemeId: ThemeId? = null,
    val activeLlmConfigId: LLMConfigId?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun init(userId: UserId): Setting {
            val now = Clock.System.now()
            val defaultConfig = LLMConfig.DEFAULT
            return Setting(
                uid = userId,
                theme = AppTheme.SYSTEM,
                activeThemeId = null,
                activeLlmConfigId = defaultConfig.id,
                llmConfigs = setOf(defaultConfig),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun changeTheme(newTheme: AppTheme): Setting {
        return this.copy(
            theme = newTheme,
            updatedAt = Clock.System.now()
        )
    }

    fun applyTheme(themeId: ThemeId?): Setting {
        return this.copy(
            activeThemeId = themeId,
            updatedAt = Clock.System.now()
        )
    }

    fun saveLlmConfig(config: LLMConfig): Setting {
        validateLlmConfig(config)

        val newConfigs = llmConfigs.filterNot { it.id == config.id } + config

        return this.copy(
            llmConfigs = newConfigs.toSet(),
            activeLlmConfigId = if (newConfigs.size == 1) config.id else activeLlmConfigId,
            updatedAt = Clock.System.now()
        )
    }

    fun replaceLlmConfigs(configs: Set<LLMConfig>, activeConfigId: LLMConfigId?): Setting {
        require(configs.isNotEmpty()) { "LLM configs cannot be empty" }
        if (activeConfigId != null) {
            require(configs.any { it.id == activeConfigId }) { "Config with id $activeConfigId not found" }
        }
        configs.forEach { validateLlmConfig(it) }
        return this.copy(
            llmConfigs = configs,
            activeLlmConfigId = activeConfigId,
            updatedAt = Clock.System.now()
        )
    }

    fun removeLlmConfig(configId: LLMConfigId): Setting {
        require(llmConfigs.any { it.id == configId }) { "Config with id $configId not found" }
        require(activeLlmConfigId != configId) { "Cannot remove active LLM config" }
        val newConfigs = llmConfigs.filterNot { it.id == configId }.toSet()
        return this.copy(
            llmConfigs = newConfigs,
            updatedAt = Clock.System.now()
        )
    }

    fun selectLlmConfig(configId: LLMConfigId): Setting {
        require(llmConfigs.any { it.id == configId }) { "Config with id $configId not found" }

        return this.copy(
            activeLlmConfigId = configId,
            updatedAt = Clock.System.now()
        )
    }

    fun getActiveLlmConfig(): LLMConfig? = activeLlmConfigId?.let { id ->
        llmConfigs.find { it.id == id }
    }

    private fun validateLlmConfig(config: LLMConfig) {
        require(config.temperature in 0.0..2.0) { "Temperature must be between 0.0 and 2.0" }

        if (config.baseUrl.contains("localhost") || config.baseUrl.contains("127.0.0.1")) {
            require(config.runOnClient) { "Localhost URLs must run on client side" }
        }
    }
}
