package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface DomainEventBusPort {
    fun register(eventType: String, handler: suspend (DomainEvent) -> Unit)
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}
