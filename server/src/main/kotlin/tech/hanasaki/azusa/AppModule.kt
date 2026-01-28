package tech.hanasaki.azusa

import io.ktor.server.config.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import org.koin.core.module.Module
import org.koin.dsl.module
import tech.hanasaki.azusa.common.kernel.event.EventPublisher
import tech.hanasaki.azusa.common.kernel.event.EventSubscriber
import tech.hanasaki.azusa.common.kernel.port.OutboxProvider
import tech.hanasaki.azusa.common.platform.di.databaseModule
import tech.hanasaki.azusa.common.platform.event.bus.InMemoryEventBus
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxAdapter
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxPoller
import tech.hanasaki.azusa.common.platform.event.outbox.readOutboxPollerConfig
import tech.hanasaki.azusa.common.platform.event.outbox.repository.ExposedOutboxEventRepository
import tech.hanasaki.azusa.common.platform.event.outbox.repository.OutboxEventRepository
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.contact.contactModules
import tech.hanasaki.azusa.modules.knowledge.knowledgeModule
import tech.hanasaki.azusa.modules.notification.notificationModule
import tech.hanasaki.azusa.modules.plugin.pluginModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        notificationModule(config),
        authModule(config),
        settingModule(config),
        themeModule(config),
        characterModule(config),
        contactModules(),
        pluginModule(config),
        knowledgeModule(config),
        databaseModule(config),
        sharedModule(config)
    )
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun sharedModule(config: ApplicationConfig) = module {
    // 事件总线
    single { InMemoryEventBus() }
    single<EventPublisher> { get<InMemoryEventBus>() }
    single<EventSubscriber> { get<InMemoryEventBus>() }

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

    // Outbox 仓储
    single<OutboxEventRepository> { ExposedOutboxEventRepository() }
    single<OutboxProvider> { OutboxAdapter(get()) }

    // Outbox 轮询器配置与实例
    single { config.readOutboxPollerConfig() }
    single {
        OutboxPoller(
            outboxRepository = get(),
            config = get(),
            redis = get(),
        )
    }

}
