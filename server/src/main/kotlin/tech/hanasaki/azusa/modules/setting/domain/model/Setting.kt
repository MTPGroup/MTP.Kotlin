package tech.hanasaki.azusa.modules.setting.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


@JvmInline
@Serializable
value class LLMConfigId(@Contextual val value: Uuid)

@Serializable
enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

@Serializable
enum class LLMProvider {
    OPENAI,
    AZURE,
    CUSTOM
}


data class Setting(
    val uid: UserId,
    val theme: AppTheme,
    val llmConfigs: Set<LLMConfig>,
    val activeThemeId: ThemeId? = null,
    val activeLlmConfigId: LLMConfigId? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun create(userId: UserId): Setting {
            val now = Clock.System.now()
            return Setting(
                uid = userId,
                theme = AppTheme.SYSTEM,
                activeThemeId = null,
                activeLlmConfigId = null,
                llmConfigs = setOf(),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun changeTheme(newTheme: AppTheme): Setting {
        return copy(
            theme = newTheme,
            updatedAt = Clock.System.now(),
        )
    }

    fun applyTheme(themeId: ThemeId?): Setting {
        return copy(
            activeThemeId = themeId,
            updatedAt = Clock.System.now(),
        )
    }

    fun saveLlmConfig(config: LLMConfig): Setting {
        validateLlmConfig(config)
        val newConfigs = llmConfigs.filterNot { it.id == config.id } + config
        return copy(
            llmConfigs = newConfigs.toSet(),
            updatedAt = Clock.System.now(),
        )
    }

    fun replaceLlmConfigs(configs: Set<LLMConfig>, activeConfigId: LLMConfigId?): Setting {
        /*if (configs.isEmpty()) {
            throw DomainException("LLM configs cannot be empty")
        }
        if (activeConfigId != null && configs.none { it.id == activeConfigId }) {
            throw DomainException("Config with id $activeConfigId not found")
        }*/
        configs.forEach { validateLlmConfig(it) }
        return copy(
            llmConfigs = configs,
            activeLlmConfigId = activeConfigId,
            updatedAt = Clock.System.now(),
        )
    }

    fun removeLlmConfig(configId: LLMConfigId): Setting {
        /*if (llmConfigs.none { it.id == configId }) {
            throw DomainException("Config with id $configId not found")
        }
        if (activeLlmConfigId == configId) {
            throw DomainException("Cannot remove active LLM config")
        }*/
        val newConfigs = llmConfigs.filterNot { it.id == configId }.toSet()
        return copy(
            llmConfigs = newConfigs,
            updatedAt = Clock.System.now(),
        )
    }

    fun selectLlmConfig(configId: LLMConfigId): Setting {
        /*if (llmConfigs.none { it.id == configId }) {
            throw DomainException("Config with id $configId not found")
        }*/
        return copy(
            activeLlmConfigId = configId,
            updatedAt = Clock.System.now(),
        )
    }

    fun getActiveLlmConfig(): LLMConfig? = activeLlmConfigId?.let { id ->
        llmConfigs.find { it.id == id }
    }

    private fun validateLlmConfig(config: LLMConfig) {
        /*if (config.temperature !in 0.0..2.0) {
            throw DomainException("Temperature must be between 0.0 and 2.0")
        }
        if (config.baseUrl.contains("localhost") || config.baseUrl.contains("127.0.0.1")) {
            if (!config.runOnClient) {
                throw DomainException("Localhost URLs must run on client side")
            }
        }*/
    }
}
