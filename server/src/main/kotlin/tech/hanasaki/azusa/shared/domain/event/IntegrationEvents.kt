package tech.hanasaki.azusa.shared.domain.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 集成事件 - 用于跨模块通信
 */
interface IntegrationEvent

@Serializable
@SerialName("OtpGenerated")
data class OtpGeneratedIntegrationEvent(
    val email: String,
    val code: String,
    val type: String,
) : IntegrationEvent
