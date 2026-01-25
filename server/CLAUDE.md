# Azusa Server - DDD Architecture Guide

## Project Overview

Azusa 是一个基于 **Ktor + Exposed + Koin** 技术栈的 Kotlin 后端服务，采用 **领域驱动设计 (DDD)** 和 **模块化架构**。

**技术栈:**

- **Web Framework:** Ktor (Netty)
- **ORM:** Exposed (Jetbrains)
- **DI:** Koin
- **Database:** PostgreSQL
- **Serialization:** Kotlinx Serialization
- **Auth:** JWT
- **Storage:** S3 Compatible
- **Migration:** Flyway

## Directory Structure

```
server/src/main/kotlin/tech/hanasaki/azusa/
├── Application.kt              # 应用入口
├── AppModule.kt                # Koin 模块聚合
├── config/                     # 配置读取
├── plugins/                    # Ktor 插件配置
│   ├── Di.kt                   # 依赖注入
│   ├── Routing.kt              # 路由配置
│   ├── Security.kt             # JWT 安全配置
│   ├── StatusPages.kt          # 异常处理
│   ├── Events.kt               # 事件系统生命周期
│   ├── ContentNegotiation.kt   # 序列化配置
│   └── ApiResponseWrapper.kt   # 统一响应包装
├── shared/                     # 共享内核 (Shared Kernel)
│   ├── domain/
│   │   ├── base/               # DDD 基础设施 (AggregateRoot)
│   │   ├── model/              # 共享值对象 (UserId, ThemeId 等)
│   │   ├── event/              # 领域事件接口
│   │   └── exception/          # 领域异常
│   ├── api/                    # 共享 API 工具
│   └── infrastructure/
│       ├── database/           # 数据库连接
│       ├── event/              # 事件系统实现
│       │   ├── config/         # 事件配置
│       │   ├── persistence/    # 事件仓储实现
│       │   └── service/        # 事件总线、轮询器
│       ├── external/           # 外部服务 (S3)
│       └── utils/              # 工具类
└── modules/                    # 业务模块
    ├── auth/                   # 认证模块
    ├── character/              # 角色模块
    ├── theme/                  # 主题模块
    └── setting/                # 设置模块
```

## Module Architecture (DDD Layers)

每个业务模块遵循分层架构：

```
modules/{module}/
├── {Module}Module.kt           # Koin 模块定义
├── {Module}Configs.kt          # 模块配置 (可选)
├── domain/                     # 领域层 (核心)
│   ├── model/                  # 实体、值对象、聚合根
│   ├── repository/             # 仓储接口
│   ├── port/                   # 端口接口 (Hexagonal)
│   └── events/                 # 领域事件
├── application/                # 应用层
│   ├── service/                # 应用服务
│   ├── command/                # 命令对象 (CQRS)
│   └── result/                 # 返回结果对象
├── infrastructure/             # 基础设施层
│   ├── persistence/
│   │   ├── table/              # Exposed 表定义
│   │   ├── mapper/             # ORM 映射器
│   │   └── repository/         # 仓储实现
│   ├── security/               # 安全实现
│   └── external/               # 外部服务实现
└── api/                        # 表现层 (Presentation)
    ├── {Module}Routes.kt       # Ktor 路由
    ├── dto/                    # 请求/响应 DTO
    └── mapper/                 # DTO 映射器
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
    val eventId: UUID
    val occurredAt: Instant
}

// modules/auth/domain/events/AuthEvents.kt
@Serializable
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now()
) : DomainEvent
```

### Repository Pattern

```kotlin
// Domain Layer - 接口定义
interface UserRepository {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
    suspend fun deleteById(id: UserId)
}

// Infrastructure Layer - 实现
class ExposedUserRepository : UserRepository {
    override suspend fun findByEmail(email: Email): User? = dbQuery { ... }
}
```

### Ports & Adapters (Hexagonal)

```kotlin
// domain/port/ - 端口定义
interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}

interface TokenService {
    fun generateTokens(userId: UserId, email: Email): TokenPair
    fun verifyRefreshToken(token: String): UserId
}

// infrastructure/security/ - 适配器实现
class PasswordEncoderImpl : PasswordEncoder { ... }
class JwtTokenService(config: JwtConfig) : TokenService { ... }
```

## Event System

采用 **Outbox Pattern** + **In-Memory Event Bus**

### 架构组件

```
shared/
├── domain/event/
│   ├── DomainEvent.kt          # 事件接口
│   ├── EventPublisher.kt       # 发布器/订阅器接口
│   └── OutboxEvent.kt          # Outbox 实体和仓储接口
└── infrastructure/event/
    ├── config/
    │   └── OutboxPollerConfig.kt   # 轮询器配置
    ├── persistence/
    │   └── ExposedOutboxEventRepository.kt
    └── service/
        ├── EventSerialization.kt   # 事件注册与序列化
        ├── InMemoryEventBus.kt     # 事件总线实现
        └── OutboxPoller.kt         # 失败事件重发轮询器
```

### 使用方式

```kotlin
// 1. 定义事件 (modules/xxx/domain/events/)
@Serializable
data class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Clock.System.now()
) : DomainEvent

// 2. 注册事件类型 (AppModule.kt)
EventRegistry.register<UserRegisteredEvent>()

// 3. 在聚合根中产生事件
class User : AggregateRoot() {
    companion object {
        fun create(...): User {
            val user = User(...)
            user.addDomainEvent(UserRegisteredEvent(user.id, email))
            return user
        }
    }
}

// 4. 在应用服务中发布事件
userRepository.save(user)
eventPublisher.publishAll(user.domainEvents)
user.clearDomainEvents()

// 5. 订阅事件
eventBus.subscribe<UserRegisteredEvent> { event ->
    sendWelcomeEmail(event.email)
}
```

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

| Module      | Description          | Aggregate Root |
|-------------|----------------------|----------------|
| `auth`      | 用户认证、注册、JWT、OTP、密码管理 | `User`         |
| `character` | AI 角色管理              | `Character`    |
| `theme`     | 主题/皮肤管理              | `Theme`        |
| `setting`   | 用户设置、LLM 配置          | `Setting`      |

## Configuration

通过 `application.yaml` + 环境变量配置：

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
```

## Coding Conventions

1. **Domain Layer 不依赖任何框架** - 保持纯 Kotlin
2. **Repository 接口在 domain 层，实现在 infrastructure 层**
3. **Port 接口在 domain/port 层，实现在 infrastructure 层**
4. **使用 value class 定义值对象** - 类型安全且零开销
5. **聚合根负责发布领域事件** - 在工厂方法或领域方法中调用 `addDomainEvent()`
6. **应用服务负责事务边界和事件发布**
7. **DTO 与领域模型完全分离** - 通过 mapper 转换
8. **所有数据库操作使用 `dbQuery { }` 包装** - 确保协程上下文正确
9. **新增事件类型需在 `AppModule.kt` 中注册**

## Adding a New Module

1. 创建目录结构 `modules/{newModule}/`
2. 定义领域模型 `domain/model/`
3. 定义仓储接口 `domain/repository/`
4. 定义端口接口 `domain/port/` (如需要)
5. 实现应用服务 `application/service/`
6. 实现基础设施 `infrastructure/persistence/`
7. 定义 API 路由 `api/`
8. 创建 Koin 模块 `{NewModule}Module.kt`
9. 在 `AppModule.kt` 中注册模块
10. 在 `Routing.kt` 中添加路由
11. 编写数据库迁移脚本 `resources/db/migration/`
12. 如有领域事件，在 `AppModule.kt` 中注册事件类型
