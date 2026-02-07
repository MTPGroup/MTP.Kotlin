package tech.hanasaki.azusa.modules.setting

import org.koin.dsl.module
import tech.hanasaki.azusa.modules.setting.adapter.`in`.event.UserRegisteredHandler
import tech.hanasaki.azusa.modules.setting.adapter.out.SettingLLMConfigProvider
import tech.hanasaki.azusa.modules.setting.adapter.out.persistence.repository.ExposedSettingRepository
import tech.hanasaki.azusa.modules.setting.application.port.`in`.SettingUseCasePort
import tech.hanasaki.azusa.modules.setting.application.service.SettingService
import tech.hanasaki.azusa.modules.setting.domain.port.SettingRepositoryPort
import tech.hanasaki.azusa.shared.infrastructure.event.onIntegrationEvent
import tech.hanasaki.azusa.shared.port.out.LLMConfigProvider

fun settingModule() = module {
    single<SettingRepositoryPort> { ExposedSettingRepository() }
    single<LLMConfigProvider> { SettingLLMConfigProvider(get()) }

    single<SettingUseCasePort> { SettingService(get(), get()) }

    onIntegrationEvent("auth.user.registered") {
        UserRegisteredHandler(get())
    }
}
