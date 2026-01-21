package tech.hanasaki.azusa.modules.auth

import io.ktor.server.config.*
import tech.hanasaki.azusa.config.requireString

data class JwtConfig(
    val issuer: String,
    val realm: String,
    val audience: String,
    val secret: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
)


data class OtpDebugConfig(
    val returnCode: Boolean,
)

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val from: String,
    val tls: Boolean,
    val enabled: Boolean,
)

fun ApplicationConfig.readJwtConfig(): JwtConfig {
    return JwtConfig(
        issuer = requireString("jwt.issuer"),
        audience = requireString("jwt.audience"),
        realm = requireString("jwt.realm"),
        secret = requireString("jwt.secret"),
        accessTokenMinutes = requireString("jwt.accessTokenMinutes").toInt(),
        refreshTokenDays = requireString("jwt.refreshTokenDays").toInt(),
    )
}


fun ApplicationConfig.readOtpDebugConfig(): OtpDebugConfig {
    return OtpDebugConfig(
        returnCode = requireString("otp.debugReturn").toBoolean(),
    )
}

fun ApplicationConfig.readSmtpConfig(): SmtpConfig {
    return SmtpConfig(
        host = requireString("smtp.host"),
        port = requireString("smtp.port").toInt(),
        username = requireString("smtp.username"),
        password = requireString("smtp.password"),
        from = requireString("smtp.from"),
        tls = requireString("smtp.tls").toBoolean(),
        enabled = requireString("smtp.enabled").toBoolean(),
    )
}
