package tech.hanasaki.azusa.modules.auth.domain.event

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


sealed class AuthEvent : DomainEvent {
    abstract val userId: UserId

    override val aggregateId: String
        get() = userId.toString()
    override val aggregateType: String
        get() = "User"
    abstract override val occurredOn: Instant
}

data class UserRegistered(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.user.registered",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email,
) : AuthEvent()

data class EmailVerified(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.email.verified",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email,
) : AuthEvent()

data class PasswordChanged(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.password.changed",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email?,
) : AuthEvent()
