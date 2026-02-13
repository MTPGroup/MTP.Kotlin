package tech.hanasaki.azusa.shared.infrastructure.llm

import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMProvider
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import org.koin.dsl.module

fun llmModule() = module {
    single<OllamaClient> { OllamaClient() }
    single<DeepSeekLLMClient> { DeepSeekLLMClient("sk-ef5a723504f147efaadaa78c0828ee73") }
    single<PromptExecutor> {
        MultiLLMPromptExecutor(
            LLMProvider.Ollama to get<OllamaClient>(),
            LLMProvider.DeepSeek to get<DeepSeekLLMClient>(),
        )
    }
    single<HttpClient> {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 5_000
            }
        }
    }
}
