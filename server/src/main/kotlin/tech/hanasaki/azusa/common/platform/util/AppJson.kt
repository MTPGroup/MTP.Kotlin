package tech.hanasaki.azusa.common.platform.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


object AppJson {

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
//        contextual(Instant::class, KotlinInstantSerializer)
            contextual(Uuid::class, UuidSerializer)
        }
    }

    object KotlinInstantSerializer : KSerializer<Instant> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)


        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Instant =
            Instant.parse(decoder.decodeString())
    }

    object UuidSerializer : KSerializer<Uuid> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Uuid) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Uuid {
            return Uuid.parse(decoder.decodeString())
        }

    }
}