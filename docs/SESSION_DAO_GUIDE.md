# SessionDao 使用指南

## 概述

`SessionDao` 是会话数据的数据库访问对象（DAO），提供了所有会话相关的 CRUD 操作。它直接操作 SQLDelight 数据库，是数据持久化的核心层。

## 架构层次

```
┌─────────────────────────────────────┐
│   SessionStore (高级缓存层)         │
│   - Store5 智能缓存                 │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│   LocalSessionDataSource (数据源)   │
│   - 业务逻辑封装                    │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│   SessionDao (数据访问层)           │ ← 你在这里
│   - 纯数据库操作                    │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│   SQLDelight Database                │
└─────────────────────────────────────┘
```

## 核心功能

### 1. 查询操作

#### 获取所有会话

```kotlin
val sessionDao: SessionDao = get()

// 同步获取
val sessions = sessionDao.getAll()

// 响应式 Flow
sessionDao.getAllAsFlow().collect { sessions ->
    println("当前有 ${sessions.size} 个会话")
}
```

#### 根据 Token 查询

```kotlin
// 同步查询
val session = sessionDao.getByToken("auth_token_123")

// 响应式 Flow
sessionDao.getByTokenAsFlow("auth_token_123").collect { session ->
    if (session != null) {
        println("找到会话: ${session.id}")
    }
}
```

#### 根据用户 ID 查询

```kotlin
// 同步查询
val session = sessionDao.getByUserId("user_123")

// 响应式 Flow
sessionDao.getByUserIdAsFlow("user_123").collect { session ->
    session?.let {
        println("用户 ${it.userId} 的会话")
    }
}
```

#### 获取当前有效会话

```kotlin
// 自动过滤过期会话
val currentSession = sessionDao.getCurrentSession()

// 响应式监听
sessionDao.getCurrentSessionAsFlow().collect { session ->
    if (session == null) {
        // 无有效会话，需要登录
        navigateToLogin()
    } else {
        // 有效会话，继续操作
        println("当前会话: ${session.id}")
    }
}
```

### 2. 插入/更新操作

#### 插入或更新单个会话

```kotlin
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

sessionDao.insertOrReplace(session)
```

#### 批量插入或更新

```kotlin
val sessions = listOf(
    Session(...
),
Session(...),
Session(...)
)

// 使用事务批量操作，性能更好
sessionDao.insertOrReplaceAll(sessions)
```

#### 更新会话 Token

```kotlin
// 只更新 token，不改变其他字段
sessionDao.updateToken(
    id = "session_123",
    token = "new_token_xyz"
)
```

### 3. 删除操作

#### 删除指定会话

```kotlin
sessionDao.deleteById("session_123")
```

#### 删除用户的所有会话

```kotlin
// 用户登出时清理该用户的所有会话
sessionDao.deleteByUserId("user_456")
```

#### 删除所有会话

```kotlin
// 应用重置或清理缓存时使用
sessionDao.deleteAll()
```

#### 删除过期会话

```kotlin
// 定期清理过期会话，释放存储空间
sessionDao.deleteExpiredSessions()
```

### 4. 实用工具方法

#### 统计会话数量

```kotlin
val count = sessionDao.count()
println("当前有 $count 个会话")
```

#### 检查会话是否存在

```kotlin
val exists = sessionDao.existsByToken("auth_token_123")
if (exists) {
    println("会话存在")
}
```

## 实战示例

### 示例 1: 登录后保存会话

```kotlin
class AuthService(
    private val sessionDao: SessionDao
) {
    suspend fun handleLoginSuccess(authResponse: AuthResponse) {
        val session = Session(
            id = authResponse.sessionId,
            token = authResponse.token,
            expiresAt = Instant.parse(authResponse.expiresAt),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            ipAddress = authResponse.ipAddress,
            userAgent = "MomoTalkPlus/1.0",
            userId = authResponse.userId
        )

        // 保存到数据库
        sessionDao.insertOrReplace(session)

        println("会话已保存")
    }
}
```

### 示例 2: 应用启动时检查登录状态

```kotlin
class SplashViewModel(
    private val sessionDao: SessionDao
) : ViewModel() {

    val loginState: StateFlow<LoginState> = sessionDao
        .getCurrentSessionAsFlow()
        .map { session ->
            when {
                session == null -> LoginState.LoggedOut
                session.isExpired() -> LoginState.Expired
                else -> LoginState.LoggedIn(session)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = LoginState.Loading
        )
}

sealed interface LoginState {
    object Loading : LoginState
    object LoggedOut : LoginState
    object Expired : LoginState
    data class LoggedIn(val session: Session) : LoginState
}
```

### 示例 3: 定期清理过期会话

```kotlin
class CleanupWorker(
    private val sessionDao: SessionDao
) {
    fun scheduleCleanup() {
        // 使用协程或后台任务
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                // 每小时清理一次
                delay(1.hours)

                val beforeCount = sessionDao.count()
                sessionDao.deleteExpiredSessions()
                val afterCount = sessionDao.count()

                val cleaned = beforeCount - afterCount
                println("清理了 $cleaned 个过期会话")
            }
        }
    }
}
```

### 示例 4: 多设备会话管理

```kotlin
class SessionManager(
    private val sessionDao: SessionDao
) {
    /**
     * 获取用户的所有活跃会话
     */
    fun getUserActiveSessions(userId: String): Flow<List<Session>> {
        return sessionDao.getAllAsFlow()
            .map { sessions ->
                sessions.filter {
                    it.userId == userId && it.isValid()
                }
            }
    }

    /**
     * 强制登出所有设备
     */
    suspend fun logoutAllDevices(userId: String) {
        sessionDao.deleteByUserId(userId)
        println("已登出用户 $userId 的所有设备")
    }

    /**
     * 登出其他设备（保留当前设备）
     */
    suspend fun logoutOtherDevices(userId: String, currentSessionId: String) {
        val allSessions = sessionDao.getAll()

        allSessions
            .filter { it.userId == userId && it.id != currentSessionId }
            .forEach { session ->
                sessionDao.deleteById(session.id)
            }
    }
}
```

### 示例 5: 会话安全监控

```kotlin
class SessionSecurityMonitor(
    private val sessionDao: SessionDao
) {
    /**
     * 检测异常登录活动
     */
    fun monitorSuspiciousActivity(userId: String): Flow<SecurityAlert?> {
        return sessionDao.getAllAsFlow()
            .map { sessions ->
                val userSessions = sessions.filter { it.userId == userId }

                // 检查是否有来自不同 IP 的活跃会话
                val ipAddresses = userSessions.map { it.ipAddress }.distinct()

                if (ipAddresses.size > 3) {
                    SecurityAlert.MultipleLocations(ipAddresses.size)
                } else {
                    null
                }
            }
    }
}

sealed interface SecurityAlert {
    data class MultipleLocations(val count: Int) : SecurityAlert
}
```

### 示例 6: 会话续期

```kotlin
class SessionRenewalService(
    private val sessionDao: SessionDao,
    private val authApi: AuthApi
) {
    /**
     * 自动续期即将过期的会话
     */
    suspend fun renewSessionIfNeeded(sessionId: String) {
        val session = sessionDao.getCurrentSession() ?: return

        // 如果会话在 1 小时内过期，进行续期
        val expiresAt = session.expiresAt ?: return
        val oneHourLater = Clock.System.now().plus(1.hours)

        if (expiresAt < oneHourLater) {
            try {
                // 调用 API 续期
                val newToken = authApi.renewSession(session.token)

                // 更新本地会话
                sessionDao.updateToken(sessionId, newToken)

                println("会话已续期")
            } catch (e: Exception) {
                println("续期失败: ${e.message}")
            }
        }
    }
}
```

## 与 LocalSessionDataSource 的对比

| 特性   | SessionDao        | LocalSessionDataSource |
|------|-------------------|------------------------|
| 职责   | 纯数据库操作            | 业务逻辑封装                 |
| 事务支持 | ✅ 原生支持            | ✅ 通过 DAO               |
| 协程支持 | ✅ suspend 函数      | ✅ withContext          |
| 错误处理 | ❌ 需手动处理           | ✅ 统一封装                 |
| 数据转换 | ✅ Entity → Domain | ✅ 已处理                  |
| 使用场景 | 底层数据操作            | 业务数据访问                 |

## 最佳实践

### 1. 使用事务批量操作

```kotlin
// ❌ 不好的做法 - 多次数据库操作
sessions.forEach { session ->
    sessionDao.insertOrReplace(session)
}

// ✅ 好的做法 - 使用事务
sessionDao.insertOrReplaceAll(sessions)
```

### 2. 优先使用 Flow 进行响应式编程

```kotlin
// ✅ 推荐 - 响应式更新 UI
sessionDao.getCurrentSessionAsFlow()
    .collect { session ->
        updateUI(session)
    }
```

### 3. 定期清理过期数据

```kotlin
// 在应用启动时
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch {
            val sessionDao: SessionDao = get()
            sessionDao.deleteExpiredSessions()
        }
    }
}
```

### 4. 使用适当的调度器

```kotlin
// ✅ 数据库操作使用 IO 调度器
viewModelScope.launch(Dispatchers.IO) {
    val sessions = sessionDao.getAll()
    // ...
}
```

## 注意事项

1. **线程安全**: SessionDao 的所有方法都是线程安全的，可以在任何协程或线程中调用

2. **异常处理**: 数据库操作可能抛出异常，建议使用 try-catch 包裹

3. **性能优化**:
    - 大量数据使用 `insertOrReplaceAll()` 而不是循环调用 `insertOrReplace()`
    - 使用 Flow 而不是轮询查询
    - 定期清理过期数据

4. **数据一致性**: 在需要保证多个操作原子性时，考虑使用事务

## 总结

`SessionDao` 是会话数据持久化的基础层，提供了：

✅ **完整的 CRUD 操作**  
✅ **响应式 Flow 支持**  
✅ **事务支持**  
✅ **自动类型转换**  
✅ **线程安全**

它是构建 `LocalSessionDataSource` 和 `SessionStore` 的基础，为整个会话管理系统提供了可靠的数据持久化能力。

