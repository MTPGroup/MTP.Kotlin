package tech.hanasaki.azusa.common.kernel.event

import java.util.*
import kotlin.time.Instant

/**
 * 领域事件
 */
interface DomainEvent {
    val eventId: UUID
    val occurredAt: Instant
}
