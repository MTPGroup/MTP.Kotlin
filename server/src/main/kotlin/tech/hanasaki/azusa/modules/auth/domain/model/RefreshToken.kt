package tech.hanasaki.azusa.modules.auth.domain.model

import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class RefreshToken(
    val id: Uuid = Uuid.random(),
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val isRevoked: Boolean = false,
) {
    fun isExpired(now: Instant = Clock.System.now()): Boolean = now > expiresAt
    fun isValid(now: Instant = Clock.System.now()): Boolean = !isRevoked && !isExpired(now)
}