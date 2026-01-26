package tech.hanasaki.azusa.modules.auth.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * OTP 生成事件 - 当 OTP 生成后发布，由通知模块订阅并发送
 */
@Serializable
data class OtpGeneratedEvent(
    val email: Email,
    val code: String,
    val otpType: OtpType,
    val expiresInMinutes: Int,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
