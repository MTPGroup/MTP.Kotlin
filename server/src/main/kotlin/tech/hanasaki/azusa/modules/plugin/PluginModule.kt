package tech.hanasaki.azusa.modules.plugin

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.plugin.application.service.PluginService
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginRepository
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository.ExposedPluginRepository
import tech.hanasaki.azusa.shared.port.out.EventPublisherPort

fun pluginModule(config: ApplicationConfig) = module {
    single<PluginRepository> { ExposedPluginRepository() }
    factory { PluginService(get(), get(), get<EventPublisherPort>()) }
}
