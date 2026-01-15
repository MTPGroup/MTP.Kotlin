package tech.hanasaki.azusa.di

import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.infrastructure.event.InMemoryEventBus

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        appModule(config),
        authModule(config),
        databaseModule(config),
        sharedModule(config)
    )
}

fun appModule(config: ApplicationConfig) = module {
}

fun sharedModule(config: ApplicationConfig) = module {
    single<EventPublisher> { InMemoryEventBus() }
}
