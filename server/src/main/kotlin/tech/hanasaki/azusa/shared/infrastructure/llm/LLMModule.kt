package tech.hanasaki.azusa.shared.infrastructure.llm

import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import org.koin.dsl.module

fun llmModule() = module {
    single<OllamaClient> { OllamaClient() }
    single<PromptExecutor> { SingleLLMPromptExecutor(get<OllamaClient>()) }
    single<HttpClient> {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 5_000
            }
        }
    }
}
