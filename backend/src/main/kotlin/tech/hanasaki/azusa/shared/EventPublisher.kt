package tech.hanasaki.azusa.shared

interface EventPublisher {
    fun publish(event: DomainEvent)
    fun publishAll(events: Collection<DomainEvent>)
}