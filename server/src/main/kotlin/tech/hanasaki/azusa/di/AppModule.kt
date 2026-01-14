package tech.hanasaki.azusa.di

import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.config.JwtConfig
import tech.hanasaki.azusa.config.readJwtConfig
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.application.service.TokenService
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.UserRepositoryImpl
import tech.hanasaki.azusa.modules.auth.infrastructure.security.JwtTokenService
import tech.hanasaki.azusa.modules.auth.infrastructure.security.PasswordEncoderImpl
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

fun authModule(config: ApplicationConfig) = module {
    single<JwtConfig> { config.readJwtConfig() }
    single<PasswordEncoder> { PasswordEncoderImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<TokenService> { JwtTokenService(get()) }

    factoryOf(::AuthService)
}

fun sharedModule(config: ApplicationConfig) = module {
    single<EventPublisher> { InMemoryEventBus() }
}
