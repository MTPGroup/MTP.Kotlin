package tech.hanasaki.azusa.common.kernel.base

import tech.hanasaki.azusa.common.kernel.event.DomainEvent

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