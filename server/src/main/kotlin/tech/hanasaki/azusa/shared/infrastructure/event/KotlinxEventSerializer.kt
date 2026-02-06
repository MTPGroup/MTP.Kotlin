package tech.hanasaki.azusa.shared.infrastructure.event

import kotlinx.serialization.json.Json
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort

class KotlinxEventSerializer(
    private val json: Json,
) : EventSerializerPort {
    override fun serialize(event: DomainEvent): String =
        json.encodeToString(event)

    override fun deserialize(payload: String): DomainEvent =
        json.decodeFromString<DomainEvent>(payload)
}