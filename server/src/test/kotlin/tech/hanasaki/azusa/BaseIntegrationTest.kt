package tech.hanasaki.azusa

import com.redis.testcontainers.RedisContainer
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.ktor.plugin.Koin
import org.testcontainers.postgresql.PostgreSQLContainer
import tech.hanasaki.azusa.bootstrap.configureCors
import tech.hanasaki.azusa.bootstrap.configureSerialization
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.authRoutes
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.configureSecurity
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.LoginResponse
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.SignInWithPasswordRequest
import tech.hanasaki.azusa.modules.auth.application.port.out.PasswordEncoderPort
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.modules.auth.domain.port.UserRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.infrastructure.config.DatabaseConfig
import tech.hanasaki.azusa.shared.infrastructure.event.InMemoryDomainEventBus
import tech.hanasaki.azusa.shared.infrastructure.event.IntegrationEventSerializersModule
import tech.hanasaki.azusa.shared.infrastructure.event.KotlinxEventSerializer
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.ExposedOutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxAdapter
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxPoller
import tech.hanasaki.azusa.shared.infrastructure.event.outbox.OutboxPollerConfig
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamEventPublisher
import tech.hanasaki.azusa.shared.infrastructure.event.redis.RedisStreamListener
import tech.hanasaki.azusa.shared.infrastructure.event.redis.StreamConfig
import tech.hanasaki.azusa.shared.infrastructure.persistence.DatabaseFactory
import tech.hanasaki.azusa.shared.infrastructure.persistence.ExposedTransactionAdapter
import tech.hanasaki.azusa.shared.infrastructure.security.securityModule
import tech.hanasaki.azusa.shared.infrastructure.web.error.configureErrorHandling
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.port.`in`.EventSubscriberPort
import tech.hanasaki.azusa.shared.port.out.*
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
        testDatasource = DatabaseFactory.init(
            DatabaseConfig(
                driver = "org.postgresql.Driver",
                url = postgresContainer.jdbcUrl,
                user = postgresContainer.username,
                password = postgresContainer.password,
                maxPoolSize = 5,
            )
        )
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
        configureErrorHandling()
        configureSecurity()
    }

    /**
     * 获取测试用的共享模块
     */
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    protected fun testEventModule(): Module = module {
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
        }.onClose {
            it?.close()
        }
        single<RedisCoroutinesCommands<String, String>> {
            get<StatefulRedisConnection<String, String>>().coroutines()
        }

        // 注册 Stream 相关组件
        single<StreamConfig> {
            StreamConfig(
                streamKey = "azusa:test:outbox-events",
                consumerGroup = "test-consumers",
                consumerName = "test-instance-1",
                batchSize = 10,
                pollInterval = 1.seconds,
            )
        }
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
        single<OutboxPollerConfig> {
            OutboxPollerConfig(
                pollingInterval = 1.seconds,
                batchSize = 10,
                cleanupEnabled = false,
                cleanupInterval = 1.seconds,
                retentionPeriod = 1.seconds,
            )
        }
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

    protected fun testDatabaseModule(): Module = module {
        single<TransactionalPort> { ExposedTransactionAdapter() }
    }

    /**
     * 子类可重写以提供额外的 Koin 模块
     */
    protected open fun additionalModules(config: ApplicationConfig): List<Module> = emptyList()

    /**
     * 子类可重写以提供额外的路由
     */
    protected open fun Route.additionalRoutes() {}

    /**
     * 通用集成测试配置
     *
     * 默认包含 testSharedModule 和 authModule，子类可通过重写 additionalModules 和 additionalRoutes 来扩展
     */
    protected fun integrationTest(
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }
        application {
            install(Koin) {
                modules(
                    testEventModule(),
                    testDatabaseModule(),
                    securityModule(),
                    authModule(environment.config),
                    *additionalModules(environment.config).toTypedArray()
                )
            }
            testModule()
            routing {
                authRoutes()
                additionalRoutes()
            }
        }
        client = createClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
        }
        startApplication()
        createTestUser()
        block()
    }

    /**
     * 创建测试用户
     */
    protected suspend fun createTestUser() {
        val koin = GlobalContext.get()
        val userRepository = koin.get<UserRepositoryPort>()
        val passwordEncoder = koin.get<PasswordEncoderPort>()
        val tx = koin.get<TransactionalPort>()
        val user = User.create(
            email = Email("test-user@example.com"),
            username = Username("Test User"),
            hashedPassword = passwordEncoder.encode(PlainPassword("password123")),
        ).apply {
            this.verifyEmail()
        }
        tx.execute {
            userRepository.save(user)
        }
    }

    protected suspend fun ClientProvider.getSignInInfo(): LoginResponse? =
        client.post("/auth/sign-in/email") {
            contentType(ContentType.Application.Json)
            setBody(
                SignInWithPasswordRequest(
                    email = "test-user@example.com",
                    password = "password123"
                )
            )
        }.body<ApiResponse<LoginResponse>>().data
}
