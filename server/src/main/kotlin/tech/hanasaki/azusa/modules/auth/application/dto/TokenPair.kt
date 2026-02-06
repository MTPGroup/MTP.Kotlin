package tech.hanasaki.azusa.modules.auth.application.dto

import kotlin.time.Instant

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshTokenExpiresAt: Instant,
)