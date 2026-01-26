package tech.hanasaki.azusa.modules.auth.domain.event

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent


@Serializable
data class EmailVerifiedEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
