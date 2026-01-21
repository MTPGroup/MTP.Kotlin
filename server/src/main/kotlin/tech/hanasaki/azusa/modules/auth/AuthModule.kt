package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.auth.application.port.EmailService
import tech.hanasaki.azusa.modules.auth.application.port.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.application.port.TokenService
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.RefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.external.EmailServiceImpl
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper.UserMapper
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedOtpRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedRefreshTokenRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository.ExposedUserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.security.JwtTokenService
import tech.hanasaki.azusa.modules.auth.infrastructure.security.PasswordEncoderImpl

fun authModule(config: ApplicationConfig) = module {
    single { UserMapper() }
    single<UserRepository> { ExposedUserRepository(get()) }
    single<RefreshTokenRepository> { ExposedRefreshTokenRepository() }
    single<OtpRepository> { ExposedOtpRepository() }
    single<JwtConfig> { config.readJwtConfig() }
    single<SmtpConfig> { config.readSmtpConfig() }
    single<PasswordEncoder> { PasswordEncoderImpl() }
    single<EmailService> { EmailServiceImpl(get()) }
    single<TokenService> { JwtTokenService(get()) }

    factoryOf(::AuthService)
    factoryOf(::OtpService)
}
