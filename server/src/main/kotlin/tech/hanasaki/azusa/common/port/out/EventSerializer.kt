package tech.hanasaki.azusa.common.port.out

import tech.hanasaki.azusa.common.domain.event.DomainEvent

interface EventSerializer {
    fun serialize(event: DomainEvent): String
    fun deserialize(payload: String): DomainEvent
}