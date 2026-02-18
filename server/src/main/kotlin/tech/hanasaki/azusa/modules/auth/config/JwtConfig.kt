package tech.hanasaki.azusa.modules.auth.config

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class JwtConfig(
    val issuer: String,
    val realm: String,
    val audience: String,
    val secret: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
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
