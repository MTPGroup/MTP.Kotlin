package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.hanasaki.azusa.common.adapter.out.event.registerDomainEvent
import tech.hanasaki.azusa.common.adapter.out.event.subscribe
import tech.hanasaki.azusa.modules.auth.adapter.`in`.event.UserRegisteredHandler
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedOtpRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedRefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository.ExposedUserRepository
import tech.hanasaki.azusa.modules.auth.adapter.out.security.JwtTokenService
import tech.hanasaki.azusa.modules.auth.adapter.out.security.PasswordEncoderImpl
import tech.hanasaki.azusa.modules.auth.config.JwtConfig
import tech.hanasaki.azusa.modules.auth.config.OtpConfig
import tech.hanasaki.azusa.modules.auth.config.readJwtConfig
import tech.hanasaki.azusa.modules.auth.config.readOtpConfig
import tech.hanasaki.azusa.modules.auth.domain.event.OtpCreated
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegistered
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCase
import tech.hanasaki.azusa.modules.auth.application.port.`in`.TokenVerifier
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.port.out.OtpRepository
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.application.port.out.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.application.port.out.TokenGenerator
import tech.hanasaki.azusa.modules.auth.application.port.out.UserRepository

fun authModule(config: ApplicationConfig) = module {
    // 仓储
    single<UserRepository> { ExposedUserRepository() }
    single<RefreshTokenRepository> { ExposedRefreshTokenRepository() }
    single<OtpRepository> { ExposedOtpRepository() }

    // 配置
    single<JwtConfig> { config.readJwtConfig() }
    single<OtpConfig> { config.readOtpConfig() }

    // 端口实现
    single<PasswordEncoder> { PasswordEncoderImpl() }
    single { JwtTokenService(get()) }
    single<TokenGenerator> { get<JwtTokenService>() }
    single<TokenVerifier> { get<JwtTokenService>() }

    // 应用服务
    singleOf(::OtpService)
    single<AuthUseCase> {
        AuthService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // 注册事件和处理器
    registerDomainEvent(UserRegistered.serializer())
    registerDomainEvent(OtpCreated.serializer())
    subscribe<UserRegistered>("auth.user.registered") {
        UserRegisteredHandler(get())
    }
}
