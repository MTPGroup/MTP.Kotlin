package tech.hanasaki.azusa.shared.domain.model.base

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort

abstract class AggregateRoot(
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf(),
) {
    val domainEvents: List<DomainEvent>
        get() = _domainEvents

    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun clearDomainEvents() {
        _domainEvents.clear()
    }
}

suspend fun AggregateRoot.publishAndClear(bus: DomainEventBusPort) {
    bus.publishAll(domainEvents)
    clearDomainEvents()
}