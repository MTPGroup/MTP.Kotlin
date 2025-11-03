package tech.hanasaki.momotalk_plus.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository

/**
 * 使用 Supabase 作为单一事实来源的会话仓库实现。
 *
 * 这个实现直接利用 supabase-kt 客户端的内置会话管理能力，
 * 观察其认证状态流来提供用户信息和登录状态。
 * 这样就无需自定义缓存（Store5）或额外的后端API（SessionApi）来管理会话。
 */
class SessionRepositoryImpl(
    private val supabase: SupabaseClient,
) : SessionRepository {

    /**
     * 观察当前登录的用户信息。
     *
     * 它通过映射 Supabase 的 `sessionStatus` 流来实现：
     * - 当用户通过身份验证时，流会发出一个 `User` 对象。
     * - 在其他状态下（如未登录、加载中），流会发出 `null`。
     */
    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun obverseUser(): Flow<User?> {
        return supabase.auth.sessionStatus
            .flatMapLatest { sessionStatus ->
                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        val userId = sessionStatus.session.user?.id
                        println("$userId")
                        supabase.from("profiles")
                            .selectSingleValueAsFlow(User::id) {
                                User::id eq userId
                            }
                    }

                    else -> flowOf(null)
                }
            }
    }

    /**
     * 观察用户的登录状态。
     *
     * - 如果用户已登录，流会发出 `true`。
     * - 否则发出 `false`。
     */
    override fun obverseLoginState(): Flow<Boolean> {
        return obverseUser().map { it != null }
    }

    /**
     * 登出用户。
     *
     * 这会调用 Supabase 的 signOut，它会清除本地存储的会话信息。
     * 由于我们的状态流直接来自 Supabase，UI 会自动更新。
     */
    override suspend fun logout() {
        supabase.auth.signOut()
    }

    /**
     * 刷新当前会话。
     *
     * 这是一个可选的辅助函数，用于在需要时强制刷新会话令牌。
     * supabase-kt 客户端通常会自动处理令牌刷新。
     */
    override suspend fun refreshCurrentSession() {
        supabase.auth.refreshCurrentSession()
    }
}