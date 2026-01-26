package tech.hanasaki.azusa.modules.auth.domain.event

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.kernel.event.DomainEvent
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import kotlin.time.Instant

/**
 * OTP 生成事件 - 跨模块集成事件
 */
@Serializable
data class OtpGeneratedEvent(
    val email: Email,
    val code: String,
    val otpType: OtpType,
    val expiresAt: Instant,
) : DomainEvent
