# SessionStore 使用指南

## 概述

`SessionStore` 是基于 [Store5](https://github.com/MobileNativeFoundation/Store)
实现的高级会话管理层，提供了智能缓存、自动刷新和会话验证功能。它结合了本地存储（SQLDelight）和远程数据源，确保会话数据的一致性和可用性。

## 架构优势

### 三层数据流

```
┌─────────────────────────────────────────┐
│         ViewModel / UI Layer            │
│         (观察会话状态变化)              │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         SessionStore (Store5)           │
│  - 智能缓存 (24小时过期)                │
│  - 自动刷新策略                         │
│  - 响应式数据流                         │
└─────┬──────────────────────┬────────────┘
      │                      │
┌─────▼────────────┐  ┌──────▼─────────────┐
│ LocalSessionData │  │ UserRemoteDataSource│
│ (SQLDelight)     │  │ (Network API)       │
└──────────────────┘  └────────────────────┘
```

### 核心特性

1. **智能缓存策略**
    - 内存缓存：24小时后自动过期
    - 本地持久化：使用 SQLDelight 存储
    - 多级缓存：内存 → 本地数据库 → 网络

2. **自动数据同步**
    - Source of Truth 模式
    - 网络数据自动写入本地
    - 本地数据优先展示

3. **会话验证**
    - 自动检测会话过期
    - 过滤无效会话
    - 智能刷新策略

## 使用示例

### 1. 在 ViewModel 中注入

```kotlin
class HomeViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    init {
        // 应用启动时清理过期会话
        viewModelScope.launch {
            sessionStore.cleanupExpiredSessions()
        }
    }
}
```

### 2. 观察会话状态变化（响应式）

```kotlin
class AuthViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    // 观察有效会话
    val sessionState: StateFlow<Session?> = sessionStore
        .observeValidSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 根据会话状态更新 UI
    val isLoggedIn: StateFlow<Boolean> = sessionState
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
```

### 3. 观察完整的 Store 响应（包含加载状态）

```kotlin
class SessionViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    val sessionResponse = sessionStore
        .observeCurrentSession()
        .map { response ->
            when (response) {
                is StoreReadResponse.Loading -> {
                    SessionUiState.Loading
                }
                is StoreReadResponse.Data -> {
                    val session = response.value
                    if (session?.isValid() == true) {
                        SessionUiState.LoggedIn(session)
                    } else {
                        SessionUiState.LoggedOut
                    }
                }
                is StoreReadResponse.Error.Exception -> {
                    SessionUiState.Error(response.error.message ?: "Unknown error")
                }
                is StoreReadResponse.Error.Message -> {
                    SessionUiState.Error(response.message)
                }
                else -> SessionUiState.LoggedOut
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionUiState.Loading
        )
}

sealed interface SessionUiState {
    object Loading : SessionUiState
    object LoggedOut : SessionUiState
    data class LoggedIn(val session: Session) : SessionUiState
    data class Error(val message: String) : SessionUiState
}
```

### 4. 强制刷新会话

```kotlin
class ProfileViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    fun refreshSession() {
        viewModelScope.launch {
            try {
                val session = sessionStore.refreshCurrentSession()
                if (session != null) {
                    // 会话刷新成功
                    _uiState.value = ProfileUiState.Success(session)
                } else {
                    // 会话不存在，跳转到登录
                    navigateToLogin()
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message)
            }
        }
    }
}
```

### 5. 按需刷新（智能缓存）

```kotlin
class DashboardViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    fun loadUserInfo() {
        viewModelScope.launch {
            // 只在缓存过期或会话无效时才刷新
            val session = sessionStore.refreshIfNeeded()

            if (session != null) {
                loadUserData(session.userId)
            } else {
                navigateToLogin()
            }
        }
    }
}
```

### 6. 检查会话有效性

```kotlin
class MainViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    fun checkLoginState() {
        viewModelScope.launch {
            val isValid = sessionStore.isSessionValid()

            if (isValid) {
                navigateToHome()
            } else {
                navigateToLogin()
            }
        }
    }
}
```

### 7. 登出清除会话

```kotlin
class SettingsViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            try {
                // 清除所有会话数据（内存缓存 + 本地数据库）
                sessionStore.clearSession()

                _uiState.value = SettingsUiState.LoggedOut
                navigateToLogin()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message)
            }
        }
    }
}
```

### 8. 获取缓存会话（不触发网络请求）

```kotlin
class SplashViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    init {
        viewModelScope.launch {
            // 快速检查本地缓存
            val cachedSession = sessionStore.getCachedSession()

            if (cachedSession?.isValid() == true) {
                // 直接进入主页
                navigateToHome()
            } else {
                // 显示登录页
                navigateToLogin()
            }
        }
    }
}
```

## 与 SessionStorageRepository 的对比

| 功能   | SessionStore         | SessionStorageRepository |
|------|----------------------|--------------------------|
| 缓存策略 | ✅ 多级缓存（内存+本地）        | ❌ 仅本地存储                  |
| 自动刷新 | ✅ 智能刷新策略             | ❌ 需手动实现                  |
| 响应式流 | ✅ Store5 响应式         | ✅ Flow                   |
| 加载状态 | ✅ Loading/Data/Error | ❌ 需手动管理                  |
| 网络集成 | ✅ 内置 Fetcher         | ❌ 需外部调用                  |
| 过期管理 | ✅ 自动过期检测             | ⚠️ 手动清理                  |
| 使用场景 | 高频访问、需缓存             | 简单 CRUD 操作               |

## 最佳实践

### 1. 应用启动时清理过期会话

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 启动 Koin
        startKoin {
            modules(appModule)
        }

        // 清理过期会话
        GlobalScope.launch {
            val sessionStore: SessionStore = get()
            sessionStore.cleanupExpiredSessions()
        }
    }
}
```

### 2. 在启动页快速检查登录状态

```kotlin
@Composable
fun SplashScreen(
    sessionStore: SessionStore,
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        val session = sessionStore.getCachedSession()

        if (session?.isValid() == true) {
            navigateToHome()
        } else {
            navigateToLogin()
        }
    }

    // 显示启动动画
    LoadingAnimation()
}
```

### 3. 全局监听登出事件

```kotlin
@Composable
fun AppNavHost(
    sessionStore: SessionStore,
    navController: NavHostController
) {
    // 监听会话状态
    val session by sessionStore
        .observeValidSession()
        .collectAsState(initial = null)

    // 会话失效时自动跳转到登录
    LaunchedEffect(session) {
        if (session == null && navController.currentDestination?.route != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController, startDestination = "splash") {
        // ...navigation graph
    }
}
```

### 4. 定期刷新会话（可选）

```kotlin
class HomeViewModel(
    private val sessionStore: SessionStore
) : ViewModel() {

    init {
        // 每小时刷新一次会话
        viewModelScope.launch {
            while (isActive) {
                delay(1.hours)
                sessionStore.refreshIfNeeded()
            }
        }
    }
}
```

## 性能优化建议

1. **使用 `observeValidSession()` 替代 `observeCurrentSession()`**
    - 自动过滤无效会话
    - 减少 UI 层的判断逻辑

2. **优先使用 `getCachedSession()`**
    - 启动页、快速检查等场景
    - 避免不必要的网络请求

3. **合理使用 `refreshIfNeeded()`**
    - 按需刷新，不强制刷新
    - 提高应用响应速度

4. **定期清理过期会话**
    - 应用启动时
    - 后台任务定期执行

## 故障排查

### 1. 会话一直显示 Loading

**原因**：网络请求失败或超时

**解决方案**：

```kotlin
val session = sessionStore
    .observeCurrentSession()
    .timeout(10.seconds) // 添加超时
    .catch { emit(StoreReadResponse.Error.Exception(it)) }
    .collectAsState(initial = StoreReadResponse.Loading())
```

### 2. 会话无法持久化

**原因**：SQLDelight 数据库初始化失败

**检查**：

- 确认 `Session.sq` 文件存在
- 运行 `./gradlew generateSqlDelightInterface`
- 检查数据库驱动工厂配置

### 3. 会话刷新过于频繁

**原因**：缓存策略配置不当

**调整**：

```kotlin
// 在 SessionStore 中修改缓存时间
.cachePolicy(
    MemoryPolicy.builder<Unit, Session?>()
        .setExpireAfterWrite(48.hours) // 增加到 48 小时
        .build()
)
```

## 总结

`SessionStore` 提供了一个强大且易用的会话管理方案，适合需要频繁访问会话数据、需要缓存优化的场景。通过 Store5
的响应式特性，您可以轻松构建一个流畅、高效的用户体验。

