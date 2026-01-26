package tech.hanasaki.azusa.modules.auth.domain.port

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Instant

/**
 * Token 服务端口 - 领域层定义的认证令牌服务接口
 */
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
