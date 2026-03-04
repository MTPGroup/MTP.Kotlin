@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.User

interface SessionRepository {
    /**
     * 获取当前用户信息
     */
    fun observeUser(): Flow<User?>


    /**
     * 获取登录状态。
     */
    fun observeLoginState(): Flow<Boolean>

    /**
     * 刷新当前会话信息
     */
    suspend fun refreshCurrentSession()

    /**
     * 主动刷新当前用户信息
     */
    suspend fun refreshCurrentUser()

    /**
     * 注销当前用户。
     */
    suspend fun logout()
}
