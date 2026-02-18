package tech.hanasaki.azusa.shared.port.`in`

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface DomainEventHandlerPort<T : DomainEvent> {
    suspend operator fun invoke(event: T)
}

