package tech.hanasaki.azusa.common.platform.event.outbox.model

import java.util.*
import kotlin.time.Instant

/**
 * Outbox 事件实体 - 用于持久化领域事件
 */
data class OutboxEvent(
    val id: UUID,
    val eventType: String,
    val payload: String,
    val occurredAt: Instant,
    val publishedAt: Instant? = null,
)
