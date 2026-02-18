package tech.hanasaki.azusa.shared.port.out

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent

interface EventSerializerPort {
    fun serialize(event: IntegrationEvent): String
    fun deserialize(payload: String): IntegrationEvent
}
