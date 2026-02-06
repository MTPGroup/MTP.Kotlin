package tech.hanasaki.azusa.shared.port.`in`

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface EventHandlerPort<T : DomainEvent> {
    suspend operator fun invoke(event: T)
}

