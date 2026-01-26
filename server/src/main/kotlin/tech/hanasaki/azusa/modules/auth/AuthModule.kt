package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.application.handler.AuthEventHandler
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.port.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.domain.port.TokenService
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.adapter.JwtTokenService
import tech.hanasaki.azusa.modules.auth.infrastructure.adapter.PasswordEncoderImpl
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
    factoryOf(::AuthService)
    factoryOf(::OtpService)

    // 事件处理器
    singleOf(::AuthEventHandler)
}
