package tech.hanasaki.azusa.shared.domain.event

interface EventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: Collection<DomainEvent>)
}