package tech.hanasaki.azusa.auth.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
)

@ConfigurationProperties(prefix = "otp")
data class OtpDebugConfig(
    val debugReturn: Boolean,
)

@ConfigurationProperties(prefix = "smtp")
data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val from: String,
    val tls: Boolean,
    val enabled: Boolean,
)