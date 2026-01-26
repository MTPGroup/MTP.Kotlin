package tech.hanasaki.azusa.common

interface EventPublisher {
    fun publish(event: DomainEvent)
    fun publishAll(events: Collection<DomainEvent>)
}