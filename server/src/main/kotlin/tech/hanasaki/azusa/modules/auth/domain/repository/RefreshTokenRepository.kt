package tech.hanasaki.azusa.modules.auth.domain.repository

import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.shared.domain.model.UserId

interface RefreshTokenRepository {
    suspend fun save(refreshToken: RefreshToken)
    suspend fun findByTokenHash(tokenHash: String): RefreshToken?
    suspend fun revoke(refreshToken: RefreshToken)
    suspend fun revokeAllForUser(userId: UserId)
}