package tech.hanasaki.azusa.modules.chat.config

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider
import tech.hanasaki.azusa.shared.infrastructure.config.optionalBoolean
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class OfficialLLMConfig(
    val provider: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
    val temperature: Double,
    val returnThinking: Boolean,
) {
    fun toLLMConfig(): LLMConfig = LLMConfig(
        provider = when (provider.lowercase()) {
            "ollama" -> LLMProvider.OLLAMA
            "openai" -> LLMProvider.OPENAI
            "alibaba" -> LLMProvider.ALIBABA
            "deepseek" -> LLMProvider.DEEPSEEK
            "google" -> LLMProvider.GOOGLE
            "anthropic" -> LLMProvider.ANTHROPIC
            else -> LLMProvider.OPENAI
        },
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
        temperature = temperature.toFloat(),
        returnThinking = returnThinking,
    )
}

fun ApplicationConfig.readOfficialLLMConfig(): OfficialLLMConfig =
    OfficialLLMConfig(
        provider = requireString("llm.provider"),
        baseUrl = requireString("llm.baseUrl"),
        model = requireString("llm.model"),
        apiKey = requireString("llm.apiKey"),
        temperature = requireString("llm.temperature").toDoubleOrNull() ?: 0.7,
        returnThinking = optionalBoolean("llm.returnThinking") ?: false,
    )