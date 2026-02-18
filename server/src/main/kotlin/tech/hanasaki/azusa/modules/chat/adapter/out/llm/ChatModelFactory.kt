package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import tech.hanasaki.azusa.modules.chat.config.OfficialLLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider

class ChatModelFactory(
    private val officialLLMConfig: OfficialLLMConfig,
) {

    fun create(config: LLMConfig): StreamingChatModel = when (config.provider) {
        LLMProvider.OFFICIAL -> with(officialLLMConfig) {
            when (provider) {
                "openai" -> OpenAiStreamingChatModel.builder()
                    .baseUrl(officialLLMConfig.baseUrl)
                    .modelName(officialLLMConfig.model)
                    .apiKey(officialLLMConfig.apiKey)
                    .temperature(officialLLMConfig.temperature)
                    .returnThinking(officialLLMConfig.returnThinking)
                    .build()

                else -> OllamaStreamingChatModel.builder()
                    .baseUrl(officialLLMConfig.baseUrl)
                    .modelName(officialLLMConfig.model)
                    .temperature(officialLLMConfig.temperature)
                    .returnThinking(officialLLMConfig.returnThinking)
                    .build()
            }
        }

        else -> OpenAiStreamingChatModel.builder()
            .baseUrl(config.baseUrl)
            .apiKey(config.apiKey)
            .modelName(config.model)
            .temperature(config.temperature.toDouble())
            .returnThinking(false)
            .build()
    }
}
