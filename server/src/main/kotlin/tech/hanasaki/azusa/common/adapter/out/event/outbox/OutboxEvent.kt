package tech.hanasaki.azusa.common.adapter.out.event.outbox

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class OutboxEvent(
    val id: Uuid = Uuid.random(),
    val aggregateType: String,
    val aggregateId: String,
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
