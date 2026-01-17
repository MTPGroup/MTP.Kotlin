package tech.hanasaki.azusa.auth.application.service

import kotlinx.datetime.Instant
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.UserId

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshTokenExpiresAt: Instant,
)

interface TokenService {
    fun generateTokens(userId: UserId, email: Email): TokenPair
    fun verifyRefreshToken(refreshToken: String): UserId
}