package tech.hanasaki.azusa.auth

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.shared.DomainEvent
import kotlin.time.Clock
import kotlin.time.Instant

data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

data class UserDeletedEvent(
    val userId: UserId,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
