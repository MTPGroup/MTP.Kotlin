package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.DomainEvent

interface EventSerializerPort {
    fun serialize(event: DomainEvent): String
    fun deserialize(payload: String): DomainEvent
}