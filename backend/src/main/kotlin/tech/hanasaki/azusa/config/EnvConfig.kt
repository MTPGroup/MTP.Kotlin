package tech.hanasaki.azusa.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "database")
data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)

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

@ConfigurationProperties(prefix = "s3")
data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val knowledgeBucket: String = bucket,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
)
