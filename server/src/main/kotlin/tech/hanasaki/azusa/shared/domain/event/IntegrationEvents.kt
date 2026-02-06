package tech.hanasaki.azusa.shared.domain.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 集成事件 - 用于跨模块通信
 */
interface IntegrationEvent {
    val eventType: String
}

@Serializable
@SerialName("OtpGenerated")
data class OtpGeneratedIntegrationEvent(
    val email: String,
    val code: String,
    val type: String,
    override val eventType: String = "auth.otp.generated",
) : IntegrationEvent

@Serializable
@SerialName("PasswordChanged")
data class PasswordChangedIntegrationEvent(
    val email: String?,
    override val eventType: String = "auth.password.changed",
) : IntegrationEvent