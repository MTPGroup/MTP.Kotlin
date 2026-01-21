package tech.hanasaki.azusa.modules.auth.domain.events

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent


data class EmailVerifiedEvent(
    val userId: UserId,
    val email: Email,
    val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
