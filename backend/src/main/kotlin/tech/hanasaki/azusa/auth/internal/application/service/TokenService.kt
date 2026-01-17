package tech.hanasaki.azusa.auth.internal.application.service

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import kotlin.time.Instant

data class TokenPair(
    val accessToken: String,
    val createdAt: Instant,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshTokenExpiresAt: Instant,
)

interface TokenService {
    fun generateTokens(userId: UserId, email: Email): TokenPair
    fun verifyRefreshToken(refreshToken: String): UserId
}