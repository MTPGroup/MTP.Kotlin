package tech.hanasaki.azusa.modules.auth.domain.event

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now(),
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
) : DomainEvent


@Serializable
data class EmailVerifiedEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now(),
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
) : DomainEvent
