package tech.hanasaki.azusa.shared.domain.model.vo

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class LLMConfigId(val value: Uuid)

@Serializable
enum class LLMProvider {
    OPENAI,
    ALIBABA,
    DEEPSEEK,
    GOOGLE,
    ANTHROPIC,
    CUSTOM
}

data class LLMConfig(
    val id: LLMConfigId = LLMConfigId(Uuid.random()),
    val provider: LLMProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val temperature: Float,
    val maxTokens: Int? = null,
    val runOnClient: Boolean = false,
)