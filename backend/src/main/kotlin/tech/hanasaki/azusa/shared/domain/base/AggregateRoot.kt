package tech.hanasaki.azusa.shared.domain.base

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

abstract class AggregateRoot(
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf<DomainEvent>(),
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