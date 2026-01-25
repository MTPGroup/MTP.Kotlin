package tech.hanasaki.azusa.shared.domain.event

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 领域事件标记接口
 */
interface DomainEvent {
    val eventId: UUID
        get() = UUID.randomUUID()
    val occurredAt: Instant
        get() = Clock.System.now()
}

/**
 * 事件元数据 - 用于持久化和传输
 */
data class EventMetadata(
    val eventId: UUID,
    val eventType: String,
    val occurredAt: Instant,
)
