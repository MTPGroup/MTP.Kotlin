package tech.hanasaki.azusa.common.platform.event.outbox.model

import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Outbox 事件实体 - 用于持久化集成事件
 */

data class OutboxEvent(
    val id: Uuid,
    val eventType: String,
    val payload: JsonElement,
    val createdAt: Instant,
    val sentAt: Instant? = null,
)

enum class OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
