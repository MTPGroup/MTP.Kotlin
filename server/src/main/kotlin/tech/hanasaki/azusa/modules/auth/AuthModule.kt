package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.common.kernel.event.SubscriptionMode
import tech.hanasaki.azusa.common.platform.event.di.subscriber
import tech.hanasaki.azusa.modules.auth.application.listener.OtpGeneratedListener
import tech.hanasaki.azusa.modules.auth.application.listener.UserRegisteredListener
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.event.OtpGeneratedEvent
import tech.hanasaki.azusa.modules.auth.domain.event.UserRegisteredEvent
import tech.hanasaki.azusa.modules.auth.domain.model.JwtConfig
import tech.hanasaki.azusa.modules.auth.domain.model.OtpConfig
import tech.hanasaki.azusa.modules.auth.domain.port.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.domain.port.TokenService
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.adapter.JwtTokenService
import tech.hanasaki.azusa.modules.auth.infrastructure.adapter.PasswordEncoderImpl
import tech.hanasaki.azusa.modules.auth.infrastructure.config.readJwtConfig
import tech.hanasaki.azusa.modules.auth.infrastructure.config.readOtpConfig
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedOtpRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedRefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedUserRepository

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
    single<TokenService> { JwtTokenService(get()) }

    // 应用服务
    factoryOf(::OtpService)
    factoryOf(::AuthService)

    // 事件监听器
    subscriber<UserRegisteredListener, UserRegisteredEvent>(
        constructor = { UserRegisteredListener(get()) },
        mode = SubscriptionMode.SYNCHRONOUS
    )

    subscriber<OtpGeneratedListener, OtpGeneratedEvent>(
        constructor = { OtpGeneratedListener(get()) },
        mode = SubscriptionMode.SYNCHRONOUS
    )
}
