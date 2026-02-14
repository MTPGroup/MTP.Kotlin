package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import tech.hanasaki.azusa.shared.domain.model.vo.LLMProvider

class ChatModelFactory {

    fun create(config: LLMConfig): StreamingChatModel = when (config.provider) {
        LLMProvider.OFFICIAL -> OllamaStreamingChatModel.builder()
            .baseUrl(config.baseUrl)
            .modelName(config.model)
            .temperature(config.temperature.toDouble())
            .returnThinking(false)
            .build()

        else -> OpenAiStreamingChatModel.builder()
            .baseUrl(config.baseUrl)
            .apiKey(config.apiKey)
            .modelName(config.model)
            .temperature(config.temperature.toDouble())
            .returnThinking(false)
            .build()
    }
}
