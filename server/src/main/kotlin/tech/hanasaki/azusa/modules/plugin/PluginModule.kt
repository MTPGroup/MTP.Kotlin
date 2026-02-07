package tech.hanasaki.azusa.modules.plugin

import org.koin.dsl.module
import tech.hanasaki.azusa.modules.plugin.adapter.out.persistence.repository.ExposedPluginLikeRepository
import tech.hanasaki.azusa.modules.plugin.adapter.out.persistence.repository.ExposedPluginRepository
import tech.hanasaki.azusa.modules.plugin.application.port.`in`.PluginUseCasePort
import tech.hanasaki.azusa.modules.plugin.application.service.PluginService
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginLikeRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort

fun pluginModule() = module {
    single<PluginRepositoryPort> { ExposedPluginRepository() }
    single<PluginLikeRepositoryPort> { ExposedPluginLikeRepository() }
    single<PluginUseCasePort> { PluginService(get(), get(), get(), get()) }
}
