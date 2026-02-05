package tech.hanasaki.azusa.common.domain.model

import tech.hanasaki.azusa.common.domain.event.DomainEvent

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