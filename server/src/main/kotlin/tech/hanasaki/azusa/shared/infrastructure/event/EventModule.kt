package tech.hanasaki.azusa.shared.infrastructure.event

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.config.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent
import tech.hanasaki.azusa.shared.infrastructure.config.RedisConfig
import tech.hanasaki.azusa.shared.infrastructure.config.readRedisConfig
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.*
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamEventPublisher
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamListener
import tech.hanasaki.azusa.shared.infrastructure.event.redis.StreamConfig
import tech.hanasaki.azusa.shared.infrastructure.event.redis.readStreamConfig
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort
import tech.hanasaki.azusa.shared.port.`in`.EventSubscriberPort
import tech.hanasaki.azusa.shared.port.`in`.IntegrationEventHandlerPort
import tech.hanasaki.azusa.shared.port.out.*


@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun eventModule(config: ApplicationConfig) = module {
    // 注册内存领域事件总线
    single<DomainEventBusPort> { InMemoryDomainEventBus() }

    // 注册集成事件(反)序列化器
    single<EventSerializerPort> {
        val partialModules: List<IntegrationEventSerializersModule> = getKoin().getAll()
        val combinedModule = partialModules.fold(SerializersModule { }) { acc, next ->
            acc + next.module
        }

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            classDiscriminator = "_type"
            serializersModule = combinedModule
        }

        KotlinxEventSerializer(json)
    }
    // 注册 Outbox 仓储
    single<OutboxEventRepositoryPort> { ExposedOutboxEventRepository() }
    // 注册 Redis 相关组件
    single<RedisConfig> { config.readRedisConfig() }
    single<RedisClient> {
        val config = get<RedisConfig>()
        val uri = RedisURI.Builder.redis(config.host, config.port)
            .withPassword(config.password)
            .withDatabase(0)
            .build()
        RedisClient.create(uri)
    }
    single<StatefulRedisConnection<String, String>> {
        get<RedisClient>().connect()
    }
    single<RedisCoroutinesCommands<String, String>> {
        get<StatefulRedisConnection<String, String>>().coroutines()
    }

    // 注册 Stream 相关组件
    single<StreamConfig> { config.readStreamConfig() }
    single<EventPublisherPort> {
        RedisStreamEventPublisher(
            get(),
            get(),
        )
    }
    single {
        RedisStreamListener(
            get(),
            get(),
            get(),
        )
    }
    single<EventSubscriberPort> { get<RedisStreamListener>() }

    // 注册 Outbox 相关组件
    single<OutboxPollerConfig> { config.readOutboxPollerConfig() }
    single<OutboxPoller> {
        OutboxPoller(
            get(),
            get(),
            get(),
            get(),
        )
    }
    single<OutboxSchedulerPort> {
        OutboxAdapter(
            get(),
            get(),
        )
    }
}

@JvmInline
value class IntegrationEventSerializersModule(val module: SerializersModule)

/**
 * 注册集成事件序列化器
 */
inline fun <reified IE : IntegrationEvent> Module.registerIntegrationEvent(
    serializer: KSerializer<IE>,
) {
    val logger = KotlinLogging.logger { }
    val qualifier = named("integration_event_serde_${IE::class.simpleName}")
    logger.info { "注册集成事件序列化器: ${IE::class.simpleName}" }

    single(qualifier) {
        IntegrationEventSerializersModule(
            SerializersModule {
                polymorphic(IntegrationEvent::class) {
                    subclass(IE::class, serializer)
                }
            }
        )
    }
}

/**
 * 注册领域事件处理器到内存总线
 */
inline fun <reified EV : DomainEvent> Module.onDomainEvent(
    eventType: String,
    crossinline handlerFactory: Scope.() -> DomainEventHandlerPort<EV>,
) {
    val name = named("domain_handler_${eventType}_${EV::class.simpleName}")
    single(qualifier = name, createdAtStart = true) {
        val logger = KotlinLogging.logger { }
        val bus = get<DomainEventBusPort>()
        val handler = handlerFactory()
        logger.info { "注册领域事件处理器: ${handler::class.simpleName} -> $eventType" }
        bus.register(eventType) { event -> if (event is EV) handler(event) }
        handler
    }
}

/**
 * 注册集成事件处理器到 Redis Stream 订阅
 */
inline fun <reified IE : IntegrationEvent> Module.onIntegrationEvent(
    eventType: String,
    crossinline handlerFactory: Scope.() -> IntegrationEventHandlerPort<IE>,
) {
    val name = named("integration_handler_${eventType}_${IE::class.simpleName}")
    single(qualifier = name, createdAtStart = true) {
        val logger = KotlinLogging.logger { }
        val subscriber = get<EventSubscriberPort>()
        val handler = handlerFactory()
        logger.info { "注册集成事件处理器: ${handler::class.simpleName} -> $eventType" }
        subscriber.registerHandler(eventType) { event -> if (event is IE) handler(event) }
        handler
    }
}
