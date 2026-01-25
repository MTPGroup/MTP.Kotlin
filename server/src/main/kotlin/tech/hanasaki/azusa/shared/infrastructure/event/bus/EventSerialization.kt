package tech.hanasaki.azusa.shared.infrastructure.event.bus

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializer
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.time.Instant

/**
 * 事件类型注册表 - 用于多态序列化/反序列化
 */
object EventRegistry {
    private val eventTypes = ConcurrentHashMap<String, KClass<out DomainEvent>>()
    private val serializers = ConcurrentHashMap<String, KSerializer<out DomainEvent>>()

    /**
     * 注册事件类型
     */
    @OptIn(InternalSerializationApi::class)
    fun <T : DomainEvent> register(kClass: kotlin.reflect.KClass<T>) {
        val typeName = kClass.qualifiedName ?: kClass.simpleName ?: error("Cannot get event type name")
        eventTypes[typeName] = kClass
        serializers[typeName] = kClass.serializer()
    }

    /**
     * 注册事件类型（内联版本）
     */
    inline fun <reified T : DomainEvent> register() {
        register(T::class)
    }

    /**
     * 批量注册事件类型
     */
    @OptIn(InternalSerializationApi::class)
    fun registerAll(vararg classes: KClass<out DomainEvent>) {
        classes.forEach { kClass ->
            val typeName = kClass.qualifiedName ?: kClass.simpleName ?: return@forEach
            eventTypes[typeName] = kClass
            serializers[typeName] = kClass.serializer()
        }
    }

    /**
     * 获取事件类型名称
     */
    fun getTypeName(event: DomainEvent): String =
        event::class.qualifiedName ?: event::class.simpleName ?: "UnknownEvent"

    /**
     * 根据类型名称获取序列化器
     */
    @Suppress("UNCHECKED_CAST")
    fun getSerializer(typeName: String): KSerializer<DomainEvent>? =
        serializers[typeName] as? KSerializer<DomainEvent>

    /**
     * 根据事件实例获取序列化器
     */
    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun getSerializer(event: DomainEvent): KSerializer<DomainEvent> {
        val typeName = getTypeName(event)
        return serializers[typeName] as? KSerializer<DomainEvent>
            ?: event::class.serializer() as KSerializer<DomainEvent>
    }

    /**
     * 获取所有已注册的事件类型
     */
    fun getRegisteredTypes(): Set<String> = eventTypes.keys.toSet()

    /**
     * 检查事件类型是否已注册
     */
    fun isRegistered(typeName: String): Boolean = eventTypes.containsKey(typeName)
}

object EventJson {
    val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(UUIDSerializer)
            contextual(InstantSerializer)
        }
    }

    /**
     * 序列化事件为 JSON
     */
    fun serialize(event: DomainEvent): String {
        val serializer = EventRegistry.getSerializer(event)
        return json.encodeToString(serializer, event)
    }

    /**
     * 反序列化 JSON 为事件
     * @param typeName 事件类型名称
     * @param payload JSON 字符串
     * @return 反序列化后的事件，如果类型未注册则返回 null
     */
    fun deserialize(typeName: String, payload: String): DomainEvent? {
        val serializer = EventRegistry.getSerializer(typeName) ?: return null
        return json.decodeFromString(serializer, payload)
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}
