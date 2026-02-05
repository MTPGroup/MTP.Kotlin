package tech.hanasaki.azusa.modules.auth.domain.event

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


@Serializable
sealed class AuthEvent : DomainEvent {
    abstract val userId: UserId

    override val aggregateId: String
        get() = userId.toString()
    override val aggregateType: String
        get() = "User"
    abstract override val occurredOn: Instant
}

@Serializable
data class UserRegistered(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.user.registered",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email,
) : AuthEvent()


@Serializable
data class EmailVerified(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.email.verified",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email,
) : AuthEvent()

