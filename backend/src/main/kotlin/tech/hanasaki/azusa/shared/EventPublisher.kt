package tech.hanasaki.azusa.shared

interface EventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: Collection<DomainEvent>)
}