# Azusa Server

Azusa 是 MomoTalk Plus (MTP) 的后端服务，采用 Kotlin + Ktor 构建，遵循领域驱动设计 (DDD) 和六边形架构。

## 项目简介

Azusa 为 MomoTalk Plus 提供完整的后端 API 服务，支持用户认证、角色管理、聊天功能、知识库、主题系统等核心功能。

## 技术栈

| 类别      | 技术                    |
|---------|-----------------------|
| Web 框架  | Ktor (Netty)          |
| ORM     | Exposed (JetBrains)   |
| 依赖注入    | Koin                  |
| 数据库     | PostgreSQL            |
| 缓存/消息队列 | Redis                 |
| 序列化     | Kotlinx Serialization |
| 认证      | JWT                   |
| 文件存储    | S3 兼容 (MinIO/RustFS)  |
| 数据库迁移   | Flyway                |
| 邮件服务    | SMTP                  |
| AI/LLM  | LangChain4j           |

## 功能模块

| 模块             | 功能描述                 | 聚合根                |
|----------------|----------------------|--------------------|
| `auth`         | 用户认证、注册、JWT、OTP、密码管理 | `User`             |
| `character`    | AI 角色管理              | `Character`        |
| `chat`         | 聊天消息、会话管理            | `Chat` / `Message` |
| `knowledge`    | 知识库文件、向量嵌入           | `KnowledgeFile`    |
| `theme`        | 主题/皮肤管理              | `Theme`            |
| `setting`      | 用户设置、LLM 配置          | `Setting`          |
| `notification` | 通知系统                 | `Notification`     |
| `plugin`       | 插件系统                 | `Plugin`           |

## 架构设计

### 六边形架构 (Ports & Adapters)

```
modules/{module}/
├── domain/                    # 领域层 (核心，无框架依赖)
│   ├── model/                 # 实体、值对象、聚合根
│   ├── port/                  # 领域端口 (仓储接口)
│   └── event/                 # 领域事件
├── application/               # 应用层
│   ├── service/               # 应用服务 (用例实现)
│   ├── port/
│   │   ├── in/                # 入站端口 (用例接口)
│   │   └── out/               # 出站端口 (技术抽象)
│   └── dto/                   # 应用层 DTO
└── adapter/
    ├── in/                    # 入站适配器
    │   ├── web/               # HTTP 路由、DTO、映射器
    │   └── event/             # 领域事件处理器
    └── out/                   # 出站适配器
        ├── persistence/       # 数据库实现 (Exposed)
        └── security/          # 安全实现
```

### 事件驱动架构

采用双通道事件系统：

- **DomainEvent** → `InMemoryDomainEventBus`（同步、事务内、模块内）
- **IntegrationEvent** → `Outbox` → `Redis Stream`（可靠、跨模块）

```
领域事件流（模块内）:
Aggregate.addDomainEvent() → publishAndClear() → InMemoryDomainEventBus → Handler

集成事件流（跨模块）:
outboxScheduler.schedule() → DB (outbox_events) → OutboxPoller → Redis Stream → Listener
```

## 快速开始

### 环境要求

- JDK 21 或更高版本
- PostgreSQL 14+
- Redis 7+
- (可选) MinIO / RustFS (S3 兼容存储)
- (可选) Docker & Docker Compose

### 配置

1. 复制环境变量模板：

```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，配置以下必需项：

```env
# 数据库
DB_URL=jdbc:postgresql://localhost:5432/azusa
DB_USER=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password

# JWT
JWT_SECRET=your_secret_key
JWT_ISSUER=azusa
JWT_AUDIENCE=azusa

# S3 存储 (MinIO)
S3_ENDPOINT=http://localhost:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin

# SMTP (可选)
SMTP_HOST=smtp.example.com
SMTP_USERNAME=noreply@example.com
SMTP_PASSWORD=your_password
```

### 启动开发依赖服务

```bash
# 使用 Docker Compose 启动 PostgreSQL、Redis、RustFS、MailDev
docker-compose up -d
```

⚠️ **注意**：`docker-compose.yml` 仅用于开发环境，包含以下服务：
- PostgreSQL (数据库)
- Redis (缓存/消息队列)
- RustFS (S3 兼容对象存储)
- MailDev (邮件测试服务)

生产环境请使用下方【部署】章节的生产部署方案。

### 构建与运行

```bash
# 运行测试
./gradlew :server:test

# 构建项目
./gradlew :server:build

# 运行开发服务器
./gradlew :server:run

# 构建可执行 Fat JAR
./gradlew :server:buildFatJar

# 构建 Docker 镜像
./gradlew :server:buildImage

# 发布镜像到本地
./gradlew :server:publishImageToLocalRegistry

# 使用 Docker 运行
./gradlew :server:runDocker
```

启动成功后，你将看到：

```
[main] INFO  Application - Application started in X.XXX seconds.
[main] INFO  Application - Responding at http://0.0.0.0:8080
```

### API 文档

启动后访问 Swagger UI：

```
http://localhost:8080/swaggerUI
```

OpenAPI 规范：

```
http://localhost:8080/swaggerUI/documention.yaml
```

## 项目结构

```
server/
├── src/
│   ├── main/kotlin/tech/hanasaki/azusa/
│   │   ├── Application.kt           # 应用入口
│   │   ├── AppModule.kt             # Koin 模块聚合
│   │   ├── bootstrap/               # Ktor 插件配置
│   │   │   ├── Di.kt               # 依赖注入
│   │   │   ├── Routing.kt          # 路由配置
│   │   │   ├── Security.kt         # JWT 安全
│   │   │   ├── StatusPages.kt      # 异常处理
│   │   │   └── ...
│   │   ├── shared/                  # 共享内核
│   │   │   ├── domain/             # DDD 基础设施
│   │   │   ├── port/               # 端口接口
│   │   │   └── infrastructure/     # 基础设施实现
│   │   └── modules/                # 业务模块
│   │       ├── auth/
│   │       ├── character/
│   │       ├── chat/
│   │       ├── knowledge/
│   │       ├── theme/
│   │       ├── setting/
│   │       ├── notification/
│   │       └── plugin/
│   └── main/resources/
│       ├── application.yaml         # 应用配置
│       ├── db/migration/           # Flyway 迁移脚本
├── build.gradle.kts                # Gradle 构建配置
├── docker-compose.yml              # 开发环境 Docker 编排
└── .env.example                    # 环境变量模板
```

## 数据库迁移

使用 Flyway 管理数据库迁移：

```bash
# 迁移脚本位于
server/src/main/resources/db/migration/

# 命名规范
V{版本号}__{描述}.sql

# 例如
V1__init.sql
V2__add_users_table.sql
```

应用启动时会自动执行未应用的迁移。

## API 响应格式

所有 API 响应统一包装：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    ...
  },
  "error": null,
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 错误处理

| 异常类型                      | HTTP 状态码 | 说明     |
|---------------------------|----------|--------|
| `NotFoundException`       | 404      | 资源不存在  |
| `ConflictException`       | 409      | 资源冲突   |
| `AuthenticationException` | 401      | 认证失败   |
| `AuthorizationException`  | 403      | 权限不足   |
| `DomainException`         | 400      | 业务规则错误 |

## 开发规范

### 代码规范

1. **领域层纯 Kotlin** — 不依赖任何框架
2. **端口命名加 `Port` 后缀** — 如 `UserRepositoryPort`
3. **使用 value class 定义值对象** — 类型安全零开销
4. **DTO 与领域模型分离** — 通过 mapper 转换
5. **数据库操作使用 `dbQuery { }` 包装**

### 领域事件使用

```kotlin
// 1. 定义领域事件
data class UserRegistered(
    override val userId: UserId,
    val email: Email,
    // ...
) : DomainEvent

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
user.publishAndClear(domainEventBus)

// 4. 注册处理器
onDomainEvent<UserRegistered>("auth.user.registered") {
    UserRegisteredHandler(get())
}
```

### 集成事件使用

```kotlin
// 1. 定义集成事件
@Serializable
@SerialName("OtpGenerated")
data class OtpGeneratedIntegrationEvent(
    val email: String,
    val code: String,
    // ...
) : IntegrationEvent

// 2. 注册序列化器
registerIntegrationEvent(OtpGeneratedIntegrationEvent.serializer())

// 3. 发布集成事件
outboxScheduler.schedule(OtpGeneratedIntegrationEvent(email, code, type))

// 4. 订阅事件
onIntegrationEvent<OtpGeneratedIntegrationEvent>("auth.otp.generated") {
    get<OtpGeneratedIntegrationListener>()::handle
}
```

## 测试

```bash
# 运行所有测试
./gradlew :server:test

# 运行特定测试类
./gradlew :server:test --tests "AuthRoutesTest"
```

测试使用 Testcontainers 提供真实的 PostgreSQL 和 Redis 环境。

## 部署

### 开发环境

开发环境使用 Docker Compose 启动依赖服务（PostgreSQL、Redis、RustFS、MailDev）：

```bash
# 1. 启动依赖服务
docker-compose up -d

# 2. 本地运行应用
./gradlew :server:run
```

**注意**：`docker-compose.yml` 仅包含开发依赖服务，不包含应用本身。生产环境请勿直接使用此配置。

### 生产环境

生产环境使用 Docker 容器部署：

```bash
# 拉取并运行
docker run -d \
  --name azusa-server \
  -p 8080:8080 \
  --env-file .env \
  hanasak1/azusa:latest
```

---

## CI/CD 自动构建

GitHub Actions 配置 (`.github/workflows/deploy-server.yml`):

| 配置项 | 说明 |
|--------|------|
| 触发条件 | 推送 `server/v*` 标签 |
| 构建平台 | `linux/amd64`, `linux/arm64` |
| 镜像地址 | `hanasak1/azusa` |
| 标签 | `latest`, `{version}` |

```bash
# 发布新版本
git tag server/v0.1.4
git push origin server/v0.1.4
```

## 许可证

[GNU General Public License v3.0](../LICENSE)

## 相关链接

- [MomoTalk Plus 主项目](../README.md)
- [Ktor 文档](https://ktor.io/docs/)
- [Exposed 文档](https://github.com/JetBrains/Exposed)
- [Koin 文档](https://insert-koin.io/)

---

**注意**: 本项目正在积极开发中，API 可能会发生变化。
