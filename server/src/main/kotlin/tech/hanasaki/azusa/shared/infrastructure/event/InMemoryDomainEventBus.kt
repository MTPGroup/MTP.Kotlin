package tech.hanasaki.azusa.shared.infrastructure.event

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort

class InMemoryDomainEventBus : DomainEventBusPort {
    private val handlers = mutableMapOf<String, MutableList<suspend (DomainEvent) -> Unit>>()

    override fun register(eventType: String, handler: suspend (DomainEvent) -> Unit) {
        handlers.computeIfAbsent(eventType) { mutableListOf() }.add(handler)
    }

    override suspend fun publish(event: DomainEvent) {
        handlers[event.eventType]?.forEach { it(event) }
    }

    override suspend fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
