package tech.hanasaki.azusa.modules.auth.domain.events

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

data class UserLoggedInEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent


data class EmailVerifiedEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

data class RefreshTokenIssuedEvent(
    val userId: UUID,
    val tokenId: UUID,
    val expiresAt: Instant,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

data class RefreshTokenRotatedEvent(
    val userId: UUID,
    val oldTokenId: UUID,
    val newTokenId: UUID,
    val expiresAt: Instant,
    val rotatedAt: Instant = Clock.System.now(),
) : DomainEvent

data class RefreshTokenRevokedEvent(
    val tokenHash: String,
    val revokedAt: Instant = Clock.System.now(),
) : DomainEvent
