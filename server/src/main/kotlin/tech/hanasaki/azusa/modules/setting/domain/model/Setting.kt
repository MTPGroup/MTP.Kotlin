package tech.hanasaki.azusa.modules.setting.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfigId
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
enum class AppTheme {
    SYSTEM, LIGHT, DARK
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

    fun replaceLlmConfigs(configs: Set<LLMConfig>, activeConfigId: LLMConfigId?): Setting {
        if (activeConfigId != null && configs.none { it.id == activeConfigId }) {
            throw ValidationException("Config with id $activeConfigId not found")
        }
        configs.forEach { validateLlmConfig(it) }
        return copy(
            llmConfigs = configs,
            activeLlmConfigId = activeConfigId,
            updatedAt = Clock.System.now(),
        )
    }

    fun getActiveLlmConfig(): LLMConfig? = activeLlmConfigId?.let { id ->
        llmConfigs.find { it.id == id }
    }

    private fun validateLlmConfig(config: LLMConfig) {
        if (config.temperature !in 0.0..2.0) {
            throw ValidationException("Temperature must be between 0.0 and 2.0")
        }
        if (config.baseUrl.contains("localhost") || config.baseUrl.contains("127.0.0.1")) {
            if (!config.runOnClient) {
                throw ValidationException("Localhost URLs must run on client side")
            }
        }
    }
}
