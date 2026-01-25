package tech.hanasaki.azusa

import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.auth.domain.events.EmailVerifiedEvent
import tech.hanasaki.azusa.modules.auth.domain.events.UserRegisteredEvent
import tech.hanasaki.azusa.shared.domain.event.integration.UserCreatedIntegrationEvent
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule
import tech.hanasaki.azusa.shared.databaseModule
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.domain.event.EventSubscriber
import tech.hanasaki.azusa.shared.domain.event.OutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.event.config.readOutboxPollerConfig
import tech.hanasaki.azusa.shared.infrastructure.event.persistence.ExposedOutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.event.service.EventRegistry
import tech.hanasaki.azusa.shared.infrastructure.event.service.InMemoryEventBus
import tech.hanasaki.azusa.shared.infrastructure.event.service.OutboxPoller

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        authModule(config),
        settingModule(config),
        themeModule(config),
        characterModule(config),
        databaseModule(config),
        sharedModule(config)
    )
}

fun sharedModule(config: ApplicationConfig) = module {
    // 注册所有领域事件类型
    EventRegistry.register<UserRegisteredEvent>()
    EventRegistry.register<EmailVerifiedEvent>()
    EventRegistry.register<UserCreatedIntegrationEvent>()

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
