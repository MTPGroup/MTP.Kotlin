package tech.hanasaki.azusa.common.adapter.out.event

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
import org.koin.dsl.onClose
import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.RedisConfig
import tech.hanasaki.azusa.common.adapter.`in`.event.RedisStreamListener
import tech.hanasaki.azusa.common.adapter.out.event.outbox.*
import tech.hanasaki.azusa.common.adapter.out.event.redis.RedisStreamEventPublisher
import tech.hanasaki.azusa.common.adapter.out.event.redis.StreamConfig
import tech.hanasaki.azusa.common.adapter.out.event.redis.readStreamConfig
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.port.`in`.EventHandler
import tech.hanasaki.azusa.common.port.`in`.EventSubscriber
import tech.hanasaki.azusa.common.port.`in`.subscribe
import tech.hanasaki.azusa.common.port.out.EventPublisher
import tech.hanasaki.azusa.common.port.out.EventSerializer
import tech.hanasaki.azusa.common.port.out.OutboxEventRepositoryPort
import tech.hanasaki.azusa.common.port.out.OutboxScheduler
import tech.hanasaki.azusa.readRedisConfig


@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun eventModule(config: ApplicationConfig) = module {
    // 注册事件(反)序列化器
    single<EventSerializer> {
        val partialModules: List<DomainEventSerializersModule> = getKoin().getAll()
        val combinedModule = partialModules.fold(SerializersModule { }) { acc, next ->
            acc + next.module
        }

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            classDiscriminator = "type"
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
    } onClose {
        it?.close()
    }
    single<RedisCoroutinesCommands<String, String>> {
        get<StatefulRedisConnection<String, String>>().coroutines()
    }

    // 注册 Stream 相关组件
    single<StreamConfig> { config.readStreamConfig() }
    single<EventPublisher> {
        RedisStreamEventPublisher(
            get(),
            get(),
            get()
        )
    }
    single {
        RedisStreamListener(
            get(),
            get(),
            get(),
        )
    }
    single<EventSubscriber> { get<RedisStreamListener>() }

    // 注册 Outbox 相关组件
    single<OutboxPollerConfig> { config.readOutboxPollerConfig() }
    single<OutboxPoller> {
        OutboxPoller(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single<OutboxScheduler> {
        OutboxAdapter(
            get(),
            get(),
        )
    }
}

@JvmInline
value class DomainEventSerializersModule(val module: SerializersModule)

inline fun <reified EV : DomainEvent> Module.registerDomainEvent(
    serializer: KSerializer<EV>,
) {
    val logger = LoggerFactory.getLogger(Module::class.java)
    val qualifier = named("event_serde_${EV::class.simpleName}")
    logger.info("注册事件序列化器: ${EV::class.simpleName}")

    single(qualifier) {
        DomainEventSerializersModule(
            SerializersModule {
                polymorphic(DomainEvent::class) {
                    subclass(EV::class, serializer)
                }
            }
        )
    }
}

inline fun <reified EV : DomainEvent> Module.subscribe(
    eventType: String,
    crossinline handlerFactory: Scope.() -> EventHandler<EV>,
) {
    val handlerName = named("subscription_${eventType}_${EV::class.simpleName}")
    single(qualifier = handlerName, createdAtStart = true) {
        val subscriber = get<EventSubscriber>()
        val handler = handlerFactory()
        subscriber.subscribe<EV>(eventType, handler)
        handler
    }
}
