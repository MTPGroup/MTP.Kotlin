package tech.hanasaki.azusa

import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.auth.domain.events.EmailVerifiedEvent
import tech.hanasaki.azusa.modules.auth.domain.events.UserRegisteredEvent
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.character.domain.events.CharacterCreatedEvent
import tech.hanasaki.azusa.modules.character.domain.events.CharacterDeletedEvent
import tech.hanasaki.azusa.modules.character.domain.events.CharacterUpdatedEvent
import tech.hanasaki.azusa.modules.contact.contactModules
import tech.hanasaki.azusa.modules.knowledge.domain.events.*
import tech.hanasaki.azusa.modules.knowledge.knowledgeModule
import tech.hanasaki.azusa.modules.plugin.domain.events.*
import tech.hanasaki.azusa.modules.plugin.pluginModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule
import tech.hanasaki.azusa.shared.databaseModule
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.event.EventSubscriber
import tech.hanasaki.azusa.shared.domain.event.OutboxEventRepository
import tech.hanasaki.azusa.shared.domain.event.integration.InitializeUserResources
import tech.hanasaki.azusa.shared.infrastructure.event.bus.EventRegistry
import tech.hanasaki.azusa.shared.infrastructure.event.bus.InMemoryEventBus
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.ExposedOutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxPoller
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.readOutboxPollerConfig

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        authModule(config),
        settingModule(config),
        themeModule(config),
        characterModule(config),
        contactModules(),
        pluginModule(config),
        knowledgeModule(config),
        databaseModule(config),
        sharedModule(config)
    )
}

fun sharedModule(config: ApplicationConfig) = module {
    // 注册所有领域事件类型
    EventRegistry.register<UserRegisteredEvent>()
    EventRegistry.register<EmailVerifiedEvent>()
    EventRegistry.register<InitializeUserResources>()
    EventRegistry.register<CharacterCreatedEvent>()
    EventRegistry.register<CharacterUpdatedEvent>()
    EventRegistry.register<CharacterDeletedEvent>()
    EventRegistry.register<PluginCreatedEvent>()
    EventRegistry.register<PluginApprovedEvent>()
    EventRegistry.register<PluginRejectedEvent>()
    EventRegistry.register<PluginSubscribedEvent>()
    EventRegistry.register<PluginUnsubscribedEvent>()
    EventRegistry.register<KnowledgeBaseCreatedEvent>()
    EventRegistry.register<KnowledgeBaseDeletedEvent>()
    EventRegistry.register<FileUploadedEvent>()
    EventRegistry.register<FileProcessedEvent>()
    EventRegistry.register<FileProcessingFailedEvent>()

    single { InMemoryEventBus() }
    single<EventPublisher> { get<InMemoryEventBus>() }
    single<EventSubscriber> { get<InMemoryEventBus>() }

    // Outbox 仓储
    single<OutboxEventRepository> { ExposedOutboxEventRepository() }

    // Outbox 轮询器配置与实例
    single { config.readOutboxPollerConfig() }
    single {
        OutboxPoller(
            outboxRepository = get(),
            eventBus = get(),
            config = get()
        )
    }
}
