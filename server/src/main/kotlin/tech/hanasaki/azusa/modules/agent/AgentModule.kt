package tech.hanasaki.azusa.modules.agent

import ai.koog.prompt.executor.ollama.client.OllamaClient
import org.koin.dsl.module

fun agentModule() = module {
    single<OllamaClient> { OllamaClient() }
}