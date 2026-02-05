package tech.hanasaki.azusa.common.adapter.out.event

import kotlinx.serialization.json.Json
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.port.out.EventSerializer

class KotlinxEventSerializer(
    private val json: Json,
) : EventSerializer {
    /*    private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            serializersModule = SerializersModule {
                polymorphic(DomainEvent::class) {
                    subclass(UserRegistered::class, UserRegistered.serializer())
                    subclass(EmailVerified::class, EmailVerified.serializer())
                }
                classDiscriminator = "type"
            }
        }*/

    override fun serialize(event: DomainEvent): String =
        json.encodeToString(event)

    override fun deserialize(payload: String): DomainEvent =
        json.decodeFromString<DomainEvent>(payload)
}