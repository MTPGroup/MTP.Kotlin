package tech.hanasaki.azusa

import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule
import tech.hanasaki.azusa.shared.databaseModule
import tech.hanasaki.azusa.shared.domain.event.EventPublisher
import tech.hanasaki.azusa.shared.infrastructure.event.InMemoryEventBus

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
    single<EventPublisher> { InMemoryEventBus() }
}
