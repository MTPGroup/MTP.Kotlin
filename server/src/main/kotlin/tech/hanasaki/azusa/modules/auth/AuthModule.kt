package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.adapter.`in`.event.UserRegisteredHandler
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedOtpRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedRefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedUserRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.security.BCryptPasswordEncoder
import tech.hanasaki.azusa.modules.auth.adapter.out.security.JwtTokenService
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCasePort
import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoderPort
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenServicePort
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.config.JwtConfig
import tech.hanasaki.azusa.modules.auth.config.OtpConfig
import tech.hanasaki.azusa.modules.auth.config.readJwtConfig
import tech.hanasaki.azusa.modules.auth.config.readOtpConfig
import tech.hanasaki.azusa.modules.auth.domain.event.OtpCreated
import tech.hanasaki.azusa.modules.auth.domain.event.PasswordChanged
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.modules.auth.domain.port.OtpRepositoryPort
import tech.hanasaki.azusa.modules.auth.domain.port.RefreshTokenRepositoryPort
import tech.hanasaki.azusa.modules.auth.domain.port.UserRepositoryPort
import tech.hanasaki.azusa.shared.infrastructure.event.registerDomainEvent
import tech.hanasaki.azusa.shared.infrastructure.event.subscribe

fun authModule(config: ApplicationConfig) = module {
    // 仓储
    single<UserRepositoryPort> { ExposedUserRepository() }
    single<RefreshTokenRepositoryPort> { ExposedRefreshTokenRepository() }
    single<OtpRepositoryPort> { ExposedOtpRepository() }

    // 配置
    single<JwtConfig> { config.readJwtConfig() }
    single<OtpConfig> { config.readOtpConfig() }

    // 端口实现
    single<PasswordEncoderPort> { BCryptPasswordEncoder() }
    single<TokenServicePort> { JwtTokenService(get()) }

    // 应用服务
    single<OtpUseCasePort> {
        OtpService(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single<AuthUseCasePort> {
        AuthService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    // 注册事件和处理器
    registerDomainEvent(UserRegistered.serializer())
    registerDomainEvent(PasswordChanged.serializer())
    registerDomainEvent(OtpCreated.serializer())
    subscribe<UserRegistered>("auth.user.registered") {
        UserRegisteredHandler(get())
    }
}
