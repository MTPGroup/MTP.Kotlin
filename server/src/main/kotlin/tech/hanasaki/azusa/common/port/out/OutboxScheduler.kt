package tech.hanasaki.azusa.common.port.out

import tech.hanasaki.azusa.common.domain.event.DomainEvent

interface OutboxScheduler {
    suspend fun schedule(event: DomainEvent)
}