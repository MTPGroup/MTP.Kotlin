package tech.hanasaki.azusa.modules.auth.infrastructure.config

import io.ktor.server.config.*
import tech.hanasaki.azusa.modules.auth.domain.model.JwtConfig
import tech.hanasaki.azusa.requireString

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

