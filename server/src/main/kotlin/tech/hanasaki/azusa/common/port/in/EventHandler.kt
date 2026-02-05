package tech.hanasaki.azusa.common.port.`in`

import tech.hanasaki.azusa.common.domain.event.DomainEvent

interface EventHandler<T : DomainEvent> {
    suspend operator fun invoke(event: T)
}

