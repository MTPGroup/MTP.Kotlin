package tech.hanasaki.azusa.auth.domain.repository

import tech.hanasaki.azusa.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.common.UserId

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken)
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun revoke(refreshToken: RefreshToken)
    fun revokeAllForUser(userId: UserId)
}