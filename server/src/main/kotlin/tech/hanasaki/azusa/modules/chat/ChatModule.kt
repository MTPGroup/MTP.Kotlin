package tech.hanasaki.azusa.modules.chat

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.chat.adapter.`in`.event.ChatCreatedHandler
import tech.hanasaki.azusa.modules.chat.adapter.out.knowledge.KnowledgeSearcherAdapter
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository.*
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatConfigUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.out.agent.AgentContextLoader
import tech.hanasaki.azusa.modules.chat.application.port.out.knowledge.KnowledgeSearcher
import tech.hanasaki.azusa.modules.chat.application.port.out.plugin.PluginToolFactory
import tech.hanasaki.azusa.modules.chat.application.service.AgentOrchestrationService
import tech.hanasaki.azusa.modules.chat.application.service.ChatConfigService
import tech.hanasaki.azusa.modules.chat.application.service.ChatService
import tech.hanasaki.azusa.modules.chat.domain.events.ChatCreated
import tech.hanasaki.azusa.modules.chat.domain.port.*
import tech.hanasaki.azusa.shared.infrastructure.event.onDomainEvent

fun chatModule(config: ApplicationConfig) = module {
    single<ChatMemberRepositoryPort> { ExposedChatMemberRepository() }
    single<ChatConfigRepositoryPort> { ExposedChatConfigRepository() }
    single<ChatPluginSubscriptionRepositoryPort> { ExposedChatPluginSubscriptionRepository() }
    single<ChatRepositoryPort> { ExposedChatRepository(get()) }
    single<MessageRepositoryPort> { ExposedMessageRepository() }

    single<ChatUseCasePort> {
        ChatService(
            chatRepository = get(),
            messageRepository = get(),
            chatMemberRepository = get(),
            chatConfigRepository = get(),
            chatPluginSubscriptionRepository = get(),
            domainEventBus = get(),
            tx = get(),
        )
    }
    single<ChatConfigUseCasePort> {
        ChatConfigService(
            chatConfigRepository = get(),
            chatPluginSubscriptionRepository = get(),
            chatRepository = get(),
            pluginRepository = get(),
            tx = get(),
        )
    }

    // Agent 编排
    single<KnowledgeSearcher> { KnowledgeSearcherAdapter(get()) }
    single { PluginToolFactory(get()) }
    single {
        AgentContextLoader(
            chatRepository = get(),
            messageRepository = get(),
            chatConfigRepository = get(),
            chatPluginSubscriptionRepository = get(),
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
            promptExecutor = get(),
            tx = get(),
        )
    }

    onDomainEvent<ChatCreated>("chat.created") {
        ChatCreatedHandler(get())
    }
}
