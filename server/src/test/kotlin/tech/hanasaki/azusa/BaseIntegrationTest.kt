package tech.hanasaki.azusa

import com.redis.testcontainers.RedisContainer
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.testcontainers.postgresql.PostgreSQLContainer
import tech.hanasaki.azusa.common.kernel.event.EventPublisher
import tech.hanasaki.azusa.common.kernel.event.EventSubscriber
import tech.hanasaki.azusa.common.kernel.event.StreamListener
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.port.OutboxProvider
import tech.hanasaki.azusa.common.platform.event.bus.InMemoryEventBus
import tech.hanasaki.azusa.common.platform.event.listener.RedisStreamListener
import tech.hanasaki.azusa.common.platform.event.listener.StreamConfig
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxAdapter
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxPoller
import tech.hanasaki.azusa.common.platform.event.outbox.OutboxPollerConfig
import tech.hanasaki.azusa.common.platform.event.outbox.repository.ExposedOutboxEventRepository
import tech.hanasaki.azusa.common.platform.event.outbox.repository.OutboxEventRepository
import tech.hanasaki.azusa.modules.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.modules.auth.domain.port.PasswordEncoder
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.plugins.configureCors
import tech.hanasaki.azusa.plugins.configureSecurity
import tech.hanasaki.azusa.plugins.configureSerialization
import tech.hanasaki.azusa.plugins.configureStatusPages
import kotlin.time.Duration.Companion.seconds

/**
 * 集成测试基类
 *
 * 提供 PostgreSQL 和 Redis testcontainer，以及测试用的 Koin 模块
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    companion object {
        private const val REDIS_PASSWORD = "redis-password"

        val postgresContainer: PostgreSQLContainer = PostgreSQLContainer("pgvector/pgvector:pg18").apply {
            withDatabaseName("azusa_test_db")
            withUsername("test_user")
            withPassword("test_pass")
        }

        val redisContainer: RedisContainer = RedisContainer("redis:8.0-bookworm").apply {
            withCommand("redis-server", "--requirepass", REDIS_PASSWORD)
        }
    }

    protected lateinit var testDatasource: HikariDataSource

    @BeforeAll
    fun setupContainers() {
        postgresContainer.start()
        redisContainer.start()

        // 运行数据库迁移
        Flyway.configure()
            .dataSource(
                postgresContainer.jdbcUrl,
                postgresContainer.username,
                postgresContainer.password
            )
            .load()
            .migrate()

        // 创建数据源
        testDatasource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgresContainer.jdbcUrl
            username = postgresContainer.username
            password = postgresContainer.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
        })

        // 连接数据库
        Database.connect(testDatasource)
    }

    @AfterAll
    fun tearDownContainers() {
        if (::testDatasource.isInitialized) {
            testDatasource.close()
        }
        postgresContainer.stop()
        redisContainer.stop()
    }

    @AfterEach
    fun cleanupKoin() {
        stopKoin()

        transaction {
            exec("truncate table public.users cascade;")
        }
    }

    /**
     * 测试配置的插件
     */
    protected fun Application.testModule() {
        configureSerialization()
        configureCors()
        configureStatusPages()
        configureSecurity()
    }

    /**
     * 获取测试用的共享模块
     */
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    protected fun testSharedModule(): Module = module {
        // 事件总线
        single { InMemoryEventBus() }
        single<EventPublisher> { get<InMemoryEventBus>() }
        single<EventSubscriber> { get<InMemoryEventBus>() }

        // Redis - 使用 testcontainer 的动态端口
        single<RedisClient> {
            val uri = RedisURI.Builder
                .redis(redisContainer.host, redisContainer.firstMappedPort)
                .withPassword(REDIS_PASSWORD.toCharArray())
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

        // Stream 配置 - 测试用
        single<StreamConfig> {
            StreamConfig(
                streamKey = "azusa:test:outbox-events",
                consumerGroup = "test-consumers",
                consumerName = "test-instance-1",
                batchSize = 10,
                pollInterval = 1.seconds,
            )
        }
        single<RedisStreamListener> {
            RedisStreamListener(redis = get(), config = get())
        }
        single<StreamListener> { get<RedisStreamListener>() }

        // Outbox Poller 配置 - 测试用
        single<OutboxPollerConfig> {
            OutboxPollerConfig(
                pollingInterval = 1.seconds,
                batchSize = 10,
                cleanupEnabled = false,
                cleanupInterval = 1.seconds,
                retentionPeriod = 1.seconds,
            )
        }
        single {
            OutboxPoller(
                outboxRepository = get(),
                outboxConfig = get(),
                streamConfig = get(),
                redis = get(),
            )
        }
    }

    /**
     * 获取所有测试模块
     */
    protected fun testModules(config: ApplicationConfig): List<Module> = listOf(
        testSharedModule(),
    )

    /**
     * 创建测试用户
     */
    protected suspend fun createTestUser() {
        val koin = GlobalContext.get()
        val userRepository = koin.get<UserRepository>()
        val passwordEncoder = koin.get<PasswordEncoder>()
        val user = User.register(
            email = Email("test-user@example.com"),
            username = Username("Test User"),
            hashedPassword = PasswordHash(
                passwordEncoder.encode("password123"),
            )
        ).apply {
            this.verifyEmail()
        }
        userRepository.save(user)
    }
}
