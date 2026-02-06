package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface OutboxSchedulerPort {
    suspend fun schedule(event: DomainEvent)
}