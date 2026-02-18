package tech.hanasaki.azusa.shared.infrastructure.event

import kotlinx.serialization.json.Json
import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort

class KotlinxEventSerializer(
    private val json: Json,
) : EventSerializerPort {
    override fun serialize(event: IntegrationEvent): String =
        json.encodeToString(event)

    override fun deserialize(payload: String): IntegrationEvent =
        json.decodeFromString<IntegrationEvent>(payload)
}
