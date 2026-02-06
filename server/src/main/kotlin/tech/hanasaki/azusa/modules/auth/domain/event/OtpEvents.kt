package tech.hanasaki.azusa.modules.auth.domain.event

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed class OtpEvent : DomainEvent {
    abstract val otpId: Uuid

    override val aggregateId: String
        get() = otpId.toString()
    override val aggregateType: String
        get() = "OTP"
}

/**
 * OTP 创建事件
 */
@Serializable
data class OtpCreated(
    val email: Email,
    val otpType: OtpType,
    val expiresAt: Instant,
    override val otpId: Uuid = Uuid.random(),
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.otp.generated",
    override val occurredOn: Instant = Clock.System.now(),
) : OtpEvent()

