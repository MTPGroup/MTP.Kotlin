package tech.hanasaki.azusa.common.domain.event

import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * 领域事件
 */

interface DomainEvent {
    val eventId: Uuid
    val occurredOn: Instant
    val aggregateId: String
    val aggregateType: String
    val eventType: String
}

