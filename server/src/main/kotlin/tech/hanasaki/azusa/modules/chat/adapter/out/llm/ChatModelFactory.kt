package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider

class ChatModelFactory(
    private val officialConfig: LLMConfig,
) {

    fun create(config: LLMConfig): StreamingChatModel {
        val resolved = resolveConfig(config)
        return when (resolved.provider) {
            LLMProvider.OLLAMA -> OllamaStreamingChatModel.builder()
                .baseUrl(resolved.baseUrl)
                .modelName(resolved.model)
                .temperature(resolved.temperature.toDouble())
                .returnThinking(resolved.returnThinking)
                .build()

            else -> OpenAiStreamingChatModel.builder()
                .baseUrl(resolved.baseUrl)
                .apiKey(resolved.apiKey)
                .modelName(resolved.model)
                .temperature(resolved.temperature.toDouble())
                .returnThinking(resolved.returnThinking)
                .build()
        }
    }

    private fun resolveConfig(config: LLMConfig): LLMConfig {
        if (config.provider != LLMProvider.OFFICIAL) return config
        return officialConfig.copy(
            temperature = config.temperature,
            maxTokens = config.maxTokens,
        )
    }
}
