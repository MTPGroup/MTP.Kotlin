package tech.hanasaki.azusa.modules.auth.application.port

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.shared.domain.model.UserId
import kotlin.time.Instant

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