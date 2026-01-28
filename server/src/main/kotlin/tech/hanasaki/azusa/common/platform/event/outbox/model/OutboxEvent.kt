package tech.hanasaki.azusa.common.platform.event.outbox.model

import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.Instant

/**
 * Outbox 事件实体 - 用于持久化集成事件
 */
data class OutboxEvent(
    val id: UUID,
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
