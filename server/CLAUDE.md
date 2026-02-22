# Azusa Server - DDD Architecture Guide

## Project Overview

Azusa 是一个基于 **Ktor + Exposed + Koin** 技术栈的 Kotlin 后端服务，采用 **领域驱动设计 (DDD)** 和 **模块化架构**。

**技术栈:**

- **Web Framework:** Ktor (Netty)
- **ORM:** Exposed (JetBrains)
- **DI:** Koin
- **Database:** PostgreSQL + pgvector
- **Cache/Message Queue:** Redis
- **Serialization:** Kotlinx Serialization
- **Auth:** JWT
- **Storage:** S3 Compatible (MinIO/RustFS)
- **Migration:** Flyway
- **Mail:** SMTP
- **AI/LLM:** LangChain4j
- **Vector DB:** pgvector (PostgreSQL extension)

## Directory Structure

```
server/src/main/kotlin/tech/hanasaki/azusa/
├── Application.kt              # 应用入口
├── bootstrap/                  # Ktor 插件配置
│   ├── Di.kt                   # 依赖注入初始化
│   ├── Routing.kt              # 路由配置
│   ├── ContentNegotiation.kt   # 序列化配置
│   └── Cors.kt                 # CORS 配置
├── shared/                     # 共享内核 (Shared Kernel)
│   ├── domain/
│   │   ├── base/               # DDD 基础设施 (AggregateRoot)
│   │   ├── model/              # 共享值对象、分页等
│   │   ├── event/              # 领域事件接口
│   │   └── exception/          # 领域异常
│   ├── port/                   # 端口接口（应用层）
│   │   ├── in/                 # 入站端口（事件处理器）
│   │   └── out/                # 出站端口（仓储、服务）
│   └── infrastructure/
│       ├── persistence/        # 数据库连接、事务
│       ├── event/              # 事件系统实现（内存总线、Outbox、Redis）
│       ├── llm/                # LLM 配置
│       ├── security/           # 安全模块（密码编码）
│       ├── storage/            # 对象存储（S3）
│       └── web/                # Web 工具（响应包装、路由助手、校验）
└── modules/                    # 业务模块
    ├── auth/                   # 认证模块（用户、JWT、OTP）
    ├── character/              # AI 角色模块
    ├── chat/                   # 聊天模块（会话、消息、Agent）
    ├── knowledge/              # 知识库模块（文档、向量搜索）
    ├── notification/           # 通知模块（邮件、推送）
    ├── plugin/                 # 插件模块
    ├── setting/                # 用户设置模块
    └── theme/                  # 主题模块
```

## Module Architecture (Hexagonal / Ports & Adapters)

每个业务模块遵循六边形架构：

```
modules/{module}/
├── {Module}Module.kt               # Koin 模块定义
├── config/                         # 模块配置 (可选)
├── domain/                         # 领域层 (核心，不依赖任何框架)
│   ├── model/                      # 实体、值对象、聚合根
│   ├── port/                       # 领域端口接口 (仓储等)
│   └── event/                      # 领域事件
├── application/                    # 应用层
│   ├── service/                    # 应用服务 (用例实现)
│   ├── port/
│   │   ├── in/                     # 入站端口 (用例接口)
│   │   └── out/                    # 出站端口 (技术抽象)
│   └── dto/                        # 应用层 DTO
├── adapter/
│   ├── in/                         # 入站适配器
│   │   ├── web/                    # HTTP 路由、请求/响应 DTO、映射器
│   │   │   ├── {Module}Routes.kt
│   │   │   ├── dto/
│   │   │   └── mapper/
│   │   └── event/                  # 领域事件处理器
│   └── out/                        # 出站适配器
│       ├── persistence/            # 数据库实现
│       │   ├── table/              # Exposed 表定义
│       │   ├── mapper/             # ORM 映射器
│       │   └── repository/         # 仓储实现
│       └── security/               # 安全实现 (可选)
```

## Core DDD Patterns

### Aggregate Root

```kotlin
// shared/domain/base/AggregateRoot.kt
abstract class AggregateRoot(
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()
) {
    val domainEvents: List<DomainEvent> get() = _domainEvents
    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }
    fun clearDomainEvents() {
        _domainEvents.clear()
    }
}
```

### Value Objects (使用 Kotlin value class)

```kotlin
// shared/domain/model/Ids.kt
@JvmInline
value class UserId(@Contextual val value: UUID)

@JvmInline
value class ThemeId(@Contextual val value: UUID)

@JvmInline
value class CharacterId(@Contextual val value: UUID)

// 模块内值对象
@JvmInline
value class Email(val value: String)

@JvmInline
value class PasswordHash(val value: String)
```

### Domain Events

```kotlin
// shared/domain/event/DomainEvent.kt
interface DomainEvent {
    val eventId: Uuid
    val occurredOn: Instant
    val aggregateId: String
    val aggregateType: String
    val eventType: String       // 用于内存总线路由，如 "auth.user.registered"
}

// modules/auth/domain/event/AuthEvents.kt — 不需要 @Serializable
sealed class AuthEvent : DomainEvent {
    abstract val userId: UserId
    override val aggregateId: String get() = userId.toString()
    override val aggregateType: String get() = "User"
}

data class UserRegistered(
    override val userId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.user.registered",
    override val occurredOn: Instant = Clock.System.now(),
    val email: Email,
) : AuthEvent()
```

### Repository Pattern (domain/port/)

```kotlin
// domain/port/ - 领域端口接口（仓储）
interface UserRepositoryPort {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
    suspend fun deleteById(id: UserId)
}

// adapter/out/persistence/repository/ - 适配器实现
class ExposedUserRepository : UserRepositoryPort { ... }
```

### Ports & Adapters (application/port/)

```kotlin
// application/port/out/ - 应用层出站端口（技术抽象）
interface PasswordEncoderPort {
    fun encode(raw: PlainPassword): HashedPassword
    fun matches(raw: PlainPassword, encoded: HashedPassword): Boolean
}

interface TokenServicePort {
    fun generate(userId: UserId, email: Email): TokenPair
    fun verify(refreshToken: String): UserId
}

// adapter/out/security/ - 适配器实现
class BCryptPasswordEncoder : PasswordEncoderPort { ... }
class JwtTokenService(config: JwtConfig) : TokenServicePort { ... }
```

## Event System

采用**双通道事件架构**：

- **DomainEvent** → `InMemoryDomainEventBus`（同步、事务内、模块内）
- **IntegrationEvent** → `Outbox` → `Redis Stream`（可靠、跨模块）

### 架构组件

```
shared/
├── domain/event/
│   ├── DomainEvent.kt              # 领域事件接口（不序列化）
│   └── IntegrationEvents.kt        # 集成事件接口 + 具体事件定义
├── port/
│   ├── out/
│   │   ├── DomainEventBusPort.kt   # 内存领域事件总线端口
│   │   ├── OutboxSchedulerPort.kt  # Outbox 调度端口（IntegrationEvent）
│   │   ├── EventPublisherPort.kt   # Redis Stream 发布端口
│   │   └── EventSerializerPort.kt  # 集成事件序列化端口
│   └── in/
│       ├── DomainEventHandlerPort.kt  # 领域事件处理器接口
│       └── EventSubscriberPort.kt     # 集成事件订阅端口
└── infrastructure/event/
    ├── InMemoryDomainEventBus.kt       # 内存领域事件总线实现
    ├── KotlinxEventSerializer.kt       # 集成事件序列化实现
    ├── EventModule.kt                  # Koin 注册 + onDomainEvent/onIntegrationEvent helpers
    ├── EventLifecycle.kt               # 事件系统生命周期
    ├── outbox/
    │   ├── OutboxAdapter.kt            # Outbox 调度实现
    │   ├── OutboxEvent.kt              # Outbox 事件实体
    │   ├── OutboxEventsTable.kt        # Exposed 表定义
    │   ├── ExposedOutboxEventRepository.kt
    │   ├── OutboxPoller.kt             # 轮询器（直接透传 eventType + payload）
    │   └── OutboxPollerConfig.kt
    └── redis/
        ├── RedisStreamEventPublisher.kt  # Redis Stream 发布
        ├── RedisStreamListener.kt        # Redis Stream 消费（实现 EventSubscriberPort）
        └── RedisEventConfig.kt
```

### 事件流

```
领域事件流（模块内，同步）:
AggregateRoot.addDomainEvent()
  → Service: publishAndClear(domainEventBus)
  → InMemoryDomainEventBus.publish()
  → DomainEventHandlerPort.invoke()

集成事件流（跨模块，可靠）:
Service: outboxScheduler.schedule(IntegrationEvent)
  → OutboxAdapter → 写入 DB (outbox_events)
  → OutboxPoller 轮询 → eventPublisher.publish(eventType, payload)
  → Redis Stream → RedisStreamListener
  → EventSubscriberPort handler
```

### 领域事件使用方式

```kotlin
// 1. 定义领域事件（不需要 @Serializable）
data class UserRegistered(
    override val userId: UserId,
    val email: Email,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "auth.user.registered",
    override val occurredOn: Instant = Clock.System.now(),
) : AuthEvent()

// 2. 聚合根中产生事件
class User : AggregateRoot() {
    companion object {
        fun create(...): User {
            val user = User(...)
            user.addDomainEvent(UserRegistered(user.id, email))
            return user
        }
    }
}

// 3. 应用服务中发布
userRepository.save(user)
user.publishAndClear(domainEventBus)  // 注入 DomainEventBusPort

// 4. 在 Module 中注册处理器
onDomainEvent<UserRegistered>("auth.user.registered") {
    UserRegisteredHandler(get())
}
```

### 集成事件使用方式

```kotlin
// 1. 定义集成事件（需要 @Serializable）
@Serializable
@SerialName("OtpGenerated")
data class OtpGeneratedIntegrationEvent(
    val email: String,
    val code: String,
    val type: String,
    override val eventType: String = "auth.otp.generated",
) : IntegrationEvent

// 2. 在生产者模块注册序列化器
registerIntegrationEvent(OtpGeneratedIntegrationEvent.serializer())

// 3. 在服务中发布集成事件
outboxScheduler.schedule(OtpGeneratedIntegrationEvent(email, code, type))

// 4. 在消费者模块订阅
onIntegrationEvent<OtpGeneratedIntegrationEvent>("auth.otp.generated") {
    val listener = get<OtpGeneratedIntegrationListener>()
    return@onIntegrationEvent { event -> listener.handle(event) }
}
```

### 设计原则

- **DomainEvent 不序列化** — 纯内存传递，不需要 `@Serializable`
- **IntegrationEvent 需序列化** — 经过 Outbox + Redis Stream，需要 `@Serializable`
- **事件命名用生产者视角** — `auth.otp.generated` 而不是 `notification.otp.generated`
- **通常在领域事件 Handler 中转换为集成事件** — 除非信息在 Handler 中已丢失（如 OTP 明文 code）

### 事件配置

```yaml
# application.yaml
event:
  outbox:
    pollingIntervalSeconds: 30
    batchSize: 100
    cleanupEnabled: true
    cleanupIntervalMinutes: 1
    retentionDays: 7
  stream:
    streamKey: azusa:outbox-events
    consumerGroup: azusa-consumers
    consumerName: instance-1
```

## Dependency Injection

使用 Koin 管理依赖：

```kotlin
// modules/auth/AuthModule.kt
fun authModule(config: ApplicationConfig) = module {
    single<UserRepository> { ExposedUserRepository() }
    single<PasswordEncoder> { PasswordEncoderImpl() }
    single<TokenService> { JwtTokenService(get()) }
    factoryOf(::AuthService)
}

// AppModule.kt - 聚合所有模块
fun appModules(config: ApplicationConfig) = listOf(
    authModule(config),
    settingModule(config),
    themeModule(config),
    characterModule(config),
    databaseModule(config),
    sharedModule(config)
)
```

## API Response Format

所有 API 响应统一包装为：

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: String
)
```

## Domain Exceptions

```kotlin
abstract class AzusaException(message: String, cause: Throwable? = null) : RuntimeException

class NotFoundException(message: String = "Resource not found") : AzusaException
class ConflictException(message: String = "Resource already exists") : AzusaException
class AuthenticationException(message: String = "Authentication failed") : AzusaException
class AuthorizationException(message: String = "Access denied") : AzusaException
class DomainException(message: String) : AzusaException
```

## Database

- **ORM:** Exposed (DAO-less, DSL style)
- **Migration:** Flyway (`resources/db/migration/V*__*.sql`)
- **Connection:** HikariCP

表定义示例：

```kotlin
object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)

    // ...
    override val primaryKey = PrimaryKey(id)
}
```

## Modules Overview

| Module         | Description          | Aggregate Root     |
|----------------|----------------------|--------------------|
| `auth`         | 用户认证、注册、JWT、OTP、密码管理 | `User`             |
| `character`    | AI 角色管理              | `Character`        |
| `chat`         | 聊天消息、会话管理            | `Chat` / `Message` |
| `knowledge`    | 知识库文件、向量嵌入           | `KnowledgeFile`    |
| `theme`        | 主题/皮肤管理              | `Theme`            |
| `setting`      | 用户设置、LLM 配置          | `Setting`          |
| `notification` | 通知系统                 | `Notification`     |
| `plugin`       | 插件系统                 | `Plugin`           |

## Configuration

通过 `.env` 文件 + `application.yaml` 配置：

### 环境变量配置

复制 `.env.example` 为 `.env` 并配置：

```bash
cp .env.example .env
```

### application.yaml 配置

```yaml
database:
  driver: "org.postgresql.Driver"
  url: ${DB_URL}
  user: ${DB_USER}
  password: ${DB_PASSWORD}

jwt:
  issuer: ${JWT_ISSUER}
  secret: ${JWT_SECRET}
  accessTokenMinutes: ${AUTH_ACCESS_TOKEN_MINUTES}
  refreshTokenDays: ${AUTH_REFRESH_TOKEN_DAYS}

s3:
  endpoint: ${S3_ENDPOINT}
  bucket: ${S3_BUCKET}

smtp:
  host: ${SMTP_HOST}
  enabled: ${SMTP_ENABLED}

event:
  outbox:
    pollingIntervalSeconds: 30
    batchSize: 100
...
```

### 开发环境配置

开发环境使用 Docker Compose 启动依赖服务：

```bash
docker-compose up -d
```

这将启动：

- PostgreSQL + pgvector
- Redis
- RustFS (S3 兼容对象存储)
- MailDev (邮件测试服务)

⚠️ **注意**：`docker-compose.yml` 仅用于开发环境，不适用于生产部署。

## Coding Conventions

1. **Domain Layer 不依赖任何框架** — 保持纯 Kotlin
2. **仓储端口在 `domain/port/`，实现在 `adapter/out/persistence/repository/`**
3. **应用层端口在 `application/port/in/`（用例）和 `application/port/out/`（技术抽象）**
4. **端口命名统一加 `Port` 后缀** — 如 `UserRepositoryPort`、`PasswordEncoderPort`、`TokenServicePort`
5. **使用 value class 定义值对象** — 类型安全且零开销
6. **聚合根负责发布领域事件** — 在工厂方法或领域方法中调用 `addDomainEvent()`
7. **应用服务负责事务边界和事件发布**
8. **DTO 与领域模型完全分离** — 通过 mapper 转换
9. **所有数据库操作使用 `dbQuery { }` 包装** — 确保协程上下文正确
10. **DomainEvent 不需要 `@Serializable`** — 纯内存传递
11. **IntegrationEvent 需要 `@Serializable`** — 经过 Outbox + Redis Stream

## Adding a New Module

1. 创建目录结构 `modules/{newModule}/`
2. 定义领域模型 `domain/model/`
3. 定义仓储端口 `domain/port/`
4. 定义应用层端口 `application/port/in/`（用例）和 `application/port/out/`（技术抽象）
5. 实现应用服务 `application/service/`
6. 实现出站适配器 `adapter/out/persistence/`
7. 定义入站适配器 `adapter/in/web/`（路由、DTO、映射器）
8. 创建 Koin 模块 `{NewModule}Module.kt`
9. 在 `AppModule.kt` 中注册模块
10. 在 `Routing.kt` 中添加路由
11. 编写数据库迁移脚本 `resources/db/migration/`
12. 如有领域事件，在模块中使用 `onDomainEvent<>()` 注册处理器
13. 如有集成事件，在模块中使用 `registerIntegrationEvent()` 和 `onIntegrationEvent<>()` 注册
