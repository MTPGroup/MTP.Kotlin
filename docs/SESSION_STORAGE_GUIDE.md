# 优雅的本地登录数据存储方案

## 概述

本项目使用 **SQLDelight** 替代 **multiplatform settings** 实现优雅的本地登录数据存储。这种方案提供了更强大的查询能力、类型安全和更好的性能。

## 架构设计

### 1. 数据层次结构

```
┌─────────────────────────────────────┐
│   Presentation Layer (ViewModel)   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Domain Layer (Repository)         │
│   - SessionStorageRepository        │
│   - UserRepository                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Data Layer                        │
│   - LocalSessionDataSource          │
│   - SqlDelightCookieStorage         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Database (SQLDelight)             │
│   - SessionEntity                   │
└─────────────────────────────────────┘
```

### 2. 核心组件

#### Session 领域模型 (`Session.kt`)

```kotlin
data class Session(
    val id: String,
    val token: String,
    val expiresAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val ipAddress: String,
    val userAgent: String,
    val userId: String
)
```

#### LocalSessionDataSource

- 负责 SQLDelight 数据库的 CRUD 操作
- 提供同步和异步查询方法
- 支持 Flow 进行响应式数据监听

#### SessionStorageRepository

- 仓库接口，定义会话存储的业务逻辑
- 提供获取当前会话、保存会话、清理过期会话等功能

#### SqlDelightCookieStorage

- 优雅地替代了 `LocalCookieStorage`
- 基于 SQLDelight 的 Cookie 存储实现
- 自动处理会话过期和验证

## 主要优势

### 与 multiplatform settings 相比

| 特性   | SQLDelight | Multiplatform Settings |
|------|------------|------------------------|
| 类型安全 | ✅ 强类型      | ⚠️ 基于 Key-Value        |
| 查询能力 | ✅ SQL 查询   | ❌ 只能按 Key 查询           |
| 复杂数据 | ✅ 支持复杂关系   | ❌ 需手动序列化               |
| 性能   | ✅ 高性能索引    | ⚠️ 简单存储                |
| 数据迁移 | ✅ 内置迁移     | ❌ 手动处理                 |
| 响应式  | ✅ Flow 支持  | ⚠️ 有限支持                |

## 使用示例

### 1. 保存会话

```kotlin
// 在登录成功后
val session = Session(
    id = "session_123",
    token = "auth_token_xyz",
    expiresAt = Clock.System.now().plus(7.days),
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    ipAddress = "192.168.1.1",
    userAgent = "MyApp/1.0",
    userId = "user_456"
)

sessionStorageRepository.saveSession(session)
```

### 2. 获取当前会话

```kotlin
// 获取当前有效会话
val currentSession = sessionStorageRepository.getCurrentSession()
if (currentSession?.isValid() == true) {
    // 用户已登录
    println("Token: ${currentSession.token}")
}
```

### 3. 监听会话变化（响应式）

```kotlin
// 在 ViewModel 中
sessionStorageRepository.observeCurrentSession()
    .collectLatest { session ->
        if (session == null) {
            // 用户已登出，导航到登录页
            navigateToLogin()
        } else {
            // 用户已登录
            updateUI(session)
        }
    }
```

### 4. 登出

```kotlin
// 清除当前会话
sessionStorageRepository.clearCurrentSession()
```

### 5. 清理过期会话

```kotlin
// 定期清理过期会话（可在应用启动时调用）
sessionStorageRepository.cleanupExpiredSessions()
```

## DI 配置

在 `AppModule.kt` 中已配置好所有依赖：

```kotlin
val storageModule = module {
    single { Settings() }
    single { createDatabaseDriverFactory() }
    single { DatabaseFactory(get()) }
    single { get<DatabaseFactory>().getSessionQueries() }

    // 新的会话存储
    single { LocalSessionDataSource(get()) }
    single<SessionStorageRepository> { SessionStorageRepositoryImpl(get()) }
    single { SqlDelightCookieStorage(get()) }
}
```

## 数据库表结构

```sql
CREATE TABLE IF NOT EXISTS SessionEntity
(
    id
    TEXT
    PRIMARY
    KEY,
    token
    TEXT
    NOT
    NULL,
    expiresAt
    TEXT,
    createdAt
    TEXT
    DEFAULT (
    datetime
(
    'now'
)),
    updatedAt TEXT DEFAULT
(
    datetime
(
    'now'
)),
    ipAddress TEXT NOT NULL,
    userAgent TEXT NOT NULL,
    userId TEXT NOT NULL
    );
```

## 迁移指南

### 从 LocalCookieStorage 迁移

旧代码：

```kotlin
class SessionRepositoryImpl(
    private val localCookieStorage: LocalCookieStorage
) {
    suspend fun logout() {
        localCookieStorage.removeCookie("better-auth.session_token")
    }
}
```

新代码：

```kotlin
class SessionRepositoryImpl(
    private val sqlDelightCookieStorage: SqlDelightCookieStorage
) {
    suspend fun logout() {
        sqlDelightCookieStorage.removeCookie("better-auth.session_token")
    }
}
```

### 从 multiplatform settings 迁移用户设置

保留 `Settings` 用于简单的用户偏好设置（如主题、通知等），使用 SQLDelight 用于复杂的会话管理。

## 最佳实践

1. **会话过期处理**：定期调用 `cleanupExpiredSessions()` 清理过期数据
2. **响应式 UI**：使用 `observeCurrentSession()` 监听登录状态变化
3. **错误处理**：检查会话的 `isValid()` 状态
4. **安全性**：敏感数据（如 token）存储在数据库中，确保平台级加密

## 性能优化

- SQLDelight 使用索引优化查询性能
- 支持批量操作
- 自动清理过期数据减少数据库大小
- Flow 支持减少不必要的查询

## 测试建议

```kotlin
@Test
fun `test session expiration`() = runTest {
        val expiredSession = Session(
            id = "test",
            token = "token",
            expiresAt = Clock.System.now().minus(1.hours),
            // ...
        )

        sessionStorageRepository.saveSession(expiredSession)
        val current = sessionStorageRepository.getCurrentSession()

        assertNull(current) // 过期会话应该被自动过滤
    }
```

## 总结

这个新的存储方案提供了：

- ✅ 类型安全的数据访问
- ✅ 强大的查询能力
- ✅ 响应式数据流
- ✅ 自动过期管理
- ✅ 跨平台支持
- ✅ 更好的性能和可维护性

