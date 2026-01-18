package tech.hanasaki.azusa.auth.internal.domain.repository

import tech.hanasaki.azusa.auth.internal.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.internal.domain.model.UserId

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken)
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun revoke(refreshToken: RefreshToken)
    fun revokeAllForUser(userId: UserId)
}