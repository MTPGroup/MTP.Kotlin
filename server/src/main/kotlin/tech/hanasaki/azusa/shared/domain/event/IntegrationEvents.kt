package tech.hanasaki.azusa.shared.domain.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * 集成事件 - 用于跨模块通信
 */
interface IntegrationEvent {
    val eventId: Uuid
    val eventType: String
}

@Serializable
@SerialName("UserRegistered")
data class UserRegisteredIntegrationEvent(
    val userId: Uuid,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.user.registered",
) : IntegrationEvent

@Serializable
@SerialName("OtpGenerated")
data class OtpGeneratedIntegrationEvent(
    val email: String,
    val code: String,
    val type: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.otp.generated",
) : IntegrationEvent

@Serializable
@SerialName("PasswordChanged")
data class PasswordChangedIntegrationEvent(
    val email: String?,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.password.changed",
) : IntegrationEvent