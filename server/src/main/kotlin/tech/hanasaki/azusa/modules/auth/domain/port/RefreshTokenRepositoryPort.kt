package tech.hanasaki.azusa.modules.auth.domain.port

import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken

/**
 * 刷新令牌仓储端口 - 被驱动端口（输出端口）
 */
interface RefreshTokenRepositoryPort {
    suspend fun save(refreshToken: RefreshToken)
    suspend fun findByTokenHash(tokenHash: String): RefreshToken?
    suspend fun revoke(refreshToken: RefreshToken)
    suspend fun revokeAllForUser(userId: UserId)
}