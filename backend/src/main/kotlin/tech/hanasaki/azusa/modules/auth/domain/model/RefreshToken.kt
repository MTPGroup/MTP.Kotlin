package tech.hanasaki.azusa.modules.auth.domain.model

import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

data class RefreshToken(
    val id: UUID = UUID.randomUUID(),
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val isRevoked: Boolean = false,
) {
    fun isExpired(now: Instant = Clock.System.now()): Boolean = now > expiresAt
    fun isValid(now: Instant = Clock.System.now()): Boolean = !isRevoked && !isExpired(now)
}
