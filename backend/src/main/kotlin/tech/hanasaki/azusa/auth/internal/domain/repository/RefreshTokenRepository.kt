package tech.hanasaki.azusa.auth.domain.repository

import tech.hanasaki.azusa.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.domain.model.UserId

interface RefreshTokenRepository {
    suspend fun save(refreshToken: RefreshToken)
    suspend fun findByTokenHash(tokenHash: String): RefreshToken?
    suspend fun revoke(refreshToken: RefreshToken)
    suspend fun revokeAllForUser(userId: UserId)
}