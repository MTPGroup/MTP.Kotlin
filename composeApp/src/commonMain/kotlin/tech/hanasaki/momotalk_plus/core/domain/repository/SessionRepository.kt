package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.User

interface SessionRepository {
    /**
     * 获取当前用户信息
     */
    fun obverseUser(): Flow<User?>


    /**
     * 获取登录状态。
     */
    fun obverseLoginState(): Flow<Boolean>

    /**
     * 刷新当前会话信息（从服务器获取最新的会话和用户数据）
     */
    suspend fun refreshCurrentSession()

    /**
     * 注销当前用户。
     */
    suspend fun logout()
}