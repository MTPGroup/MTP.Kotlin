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


data class OtpConfig(
    val testMode: Boolean = false,
    val testCode: String = "123456",
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


fun ApplicationConfig.readOtpConfig(): OtpConfig {
    return OtpConfig(
        testMode = propertyOrNull("otp.testMode")?.getString()?.toBoolean() ?: false,
        testCode = propertyOrNull("otp.testCode")?.getString() ?: "123456",
    )
}
