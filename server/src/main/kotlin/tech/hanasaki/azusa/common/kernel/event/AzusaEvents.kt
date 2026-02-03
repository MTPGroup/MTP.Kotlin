package tech.hanasaki.azusa.common.kernel.event

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 领域事件
 */

interface DomainEvent {
    val eventId: Uuid
    val occurredAt: Instant
}
