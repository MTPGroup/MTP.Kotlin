package tech.hanasaki.azusa.modules.plugin

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.plugin.application.service.PluginService
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginLikeRepository
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginRepository

import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository.ExposedPluginLikeRepository
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository.ExposedPluginRepository

import tech.hanasaki.azusa.common.kernel.event.EventPublisher

fun pluginModule(config: ApplicationConfig) = module {
    single<PluginRepository> { ExposedPluginRepository() }
    factory { PluginService(get(), get(), get<EventPublisher>()) }
}
