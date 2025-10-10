package tech.hanasaki.momotalk_plus.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.hanasaki.momotalk_plus.core.data.datasource.local.LocalCookieStorage
import tech.hanasaki.momotalk_plus.core.data.datasource.local.LocalSessionDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.SessionMapper.toSession
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.UserMapper.toUser
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.api.SessionApi
import tech.hanasaki.momotalk_plus.core.domain.model.Session
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import kotlin.time.Duration.Companion.days

class SessionRepositoryImpl(
    private val sessionApi: SessionApi,
    private val localSessionDataSource: LocalSessionDataSource,
    private val cookieStorage: LocalCookieStorage,
) : SessionRepository {
    data class SessionWithUser(
        val session: Session,
        val user: User,
    )

    /**
     * 当前会话 Store
     * 缓存策略: 7天后过期，每次访问时验证有效性
     */
    val currentSessionStore: Store<Unit, SessionWithUser> = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: Unit ->
                val response = sessionApi.getSessionInfo()
                SessionWithUser(
                    session = response.session.toSession(),
                    user = response.user.toUser()
                )
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Unit ->
                    localSessionDataSource.observeCurrentSession()
                        .map { session ->
                            session?.let {
                                val user =
                                    localSessionDataSource.getCurrentSessionWithUser()?.second
                                user?.let { SessionWithUser(session, user) }
                            }
                        }
                },
                writer = { _: Unit, data: SessionWithUser ->
                    localSessionDataSource.saveSessionWithUser(
                        data.session,
                        data.user
                    )
                },
                delete = { _: Unit ->
                    localSessionDataSource.deleteAllSessions()
                },
                deleteAll = {
                    localSessionDataSource.deleteAllSessions()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.Companion.builder<Unit, SessionWithUser>()
                .setExpireAfterWrite(7.days)
                .build()
        )
        .build()

    /**
     * 获取当前会话（包含用户）的数据流
     */
    fun observeCurrentSessionWithUser(): Flow<SessionWithUser?> {
        return currentSessionStore.stream(StoreReadRequest.Companion.cached(Unit, refresh = false))
            .map { response ->
                when (response) {
                    is StoreReadResponse.Data -> response.value
                    is StoreReadResponse.Loading -> null
                    is StoreReadResponse.Error -> {
                        println("获取会话失败: ${response.errorMessageOrNull()}")
                        null
                    }

                    is StoreReadResponse.NoNewData -> null
                    else -> null
                }
            }
    }

    override suspend fun refreshCurrentSession() {
        try {
            currentSessionStore.fresh(Unit)
        } catch (e: Exception) {
            println("刷新会话失败: ${e.message}")
            throw e
        }
    }

    /**
     * 获取缓存的当前会话
     */
    suspend fun getCachedSession(): SessionWithUser {
        return try {
            currentSessionStore.get(Unit)
        } catch (e: Exception) {
            println("获取会话失败: ${e.message}")
            throw e
        }
    }


    /**
     * 清除会话缓存和本地数据
     */
    @OptIn(ExperimentalStoreApi::class)
    suspend fun clearSession() {
        sessionApi.logout()
        currentSessionStore.clear()
        localSessionDataSource.deleteAllSessions()
    }

    /**
     * 验证当前会话是否有效
     */
    suspend fun isSessionValid(): Boolean {
        return try {
            val data = getCachedSession()
            data.session.isValid()
        } catch (e: Exception) {
            false
        }
    }


    override fun obverseUser(): Flow<User?> {
        return observeCurrentSessionWithUser()
            .map { it?.user }
    }

    override fun obverseLoginState(): Flow<Boolean> {
        return observeCurrentSessionWithUser()
            .map { sessionWithUser ->
                sessionWithUser != null && sessionWithUser.session.isValid()
            }
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun logout() {
        // 清除会话和用户数据
        currentSessionStore.clear()
        localSessionDataSource.deleteAllSessions()
        // 清除所有 Cookie（数据库）
        cookieStorage.clearAll()
    }
}