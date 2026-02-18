package tech.hanasaki.azusa.modules.chat

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.chat.adapter.out.agent.AgentContextLoader
import tech.hanasaki.azusa.modules.chat.adapter.out.knowledge.KnowledgeSearcherAdapter
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository.ExposedChatMemberRepository
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository.ExposedChatRepository
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository.ExposedMessageRepository
import tech.hanasaki.azusa.modules.chat.adapter.out.plugin.PluginToolFactory
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.out.AgentContextLoaderPort
import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolFactoryPort
import tech.hanasaki.azusa.modules.chat.application.service.AgentOrchestrationService
import tech.hanasaki.azusa.modules.chat.application.service.ChatService
import tech.hanasaki.azusa.modules.chat.config.OfficialLLMConfig
import tech.hanasaki.azusa.modules.chat.config.readOfficialLLMConfig
import tech.hanasaki.azusa.modules.chat.domain.port.ChatMemberRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort

fun chatModule(config: ApplicationConfig) = module {
    single<ChatMemberRepositoryPort> { ExposedChatMemberRepository() }
    single<ChatRepositoryPort> { ExposedChatRepository(get()) }
    single<MessageRepositoryPort> { ExposedMessageRepository() }

    single<ChatUseCasePort> {
        ChatService(
            chatRepository = get(),
            messageRepository = get(),
            chatMemberRepository = get(),
            pluginRepository = get(),
            domainEventBus = get(),
            tx = get(),
        )
    }

    single<OfficialLLMConfig> { config.readOfficialLLMConfig() }

    // Agent 编排
    single<KnowledgeSearcherPort> { KnowledgeSearcherAdapter(get()) }
    single<PluginToolFactoryPort> { PluginToolFactory(get()) }
    single<AgentContextLoaderPort> {
        AgentContextLoader(
            chatRepository = get(),
            messageRepository = get(),
            characterRepository = get(),
            knowledgeSubscriptionRepository = get(),
            pluginRepository = get(),
            knowledgeSearcher = get(),
            pluginToolFactory = get(),
            llmConfigProvider = get(),
            tx = get(),
        )
    }
    single<AgentUseCasePort> {
        AgentOrchestrationService(
            contextLoader = get(),
            chatRepository = get(),
            messageRepository = get(),
            chatModelFactory = get(),
            tx = get(),
        )
    }
}
