package tech.hanasaki.azusa.common

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