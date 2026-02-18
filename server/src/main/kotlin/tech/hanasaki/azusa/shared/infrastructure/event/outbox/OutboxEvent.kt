package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class OutboxEvent(
    val id: Uuid = Uuid.random(),
    val eventType: String,
    val payload: String,
    val status: OutboxEventStatus = OutboxEventStatus.PENDING,
    val retryCount: Int = 0,
    val createdAt: Instant = Clock.System.now(),
)

enum class OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
