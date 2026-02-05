package tech.hanasaki.azusa.modules.auth.application.result

import kotlin.time.Instant

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshTokenExpiresAt: Instant,
)
