package tech.hanasaki.azusa.modules.auth.domain.model

data class JwtConfig(
    val issuer: String,
    val realm: String,
    val audience: String,
    val secret: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
)

