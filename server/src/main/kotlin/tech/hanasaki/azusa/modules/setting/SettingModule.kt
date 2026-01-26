package tech.hanasaki.azusa.modules.setting

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.common.kernel.port.LLMConfigProvider
import tech.hanasaki.azusa.modules.setting.application.handler.SettingEventHandler
import tech.hanasaki.azusa.modules.setting.application.service.SettingService
import tech.hanasaki.azusa.modules.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.modules.setting.infrastructure.adapter.SettingLLMConfigProvider
import tech.hanasaki.azusa.modules.setting.infrastructure.persistence.repository.ExposedSettingRepository

fun settingModule(config: ApplicationConfig) = module {
    single<SettingRepository> { ExposedSettingRepository() }
    single<LLMConfigProvider> { SettingLLMConfigProvider(get()) }

    factoryOf(::SettingService)
    factoryOf(::SettingEventHandler)
}
