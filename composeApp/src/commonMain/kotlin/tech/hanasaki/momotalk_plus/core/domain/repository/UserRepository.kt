package tech.hanasaki.momotalk_plus.core.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError

interface UserRepository {
    /**
     * 获取当前用户信息
     *
     * @param uid 用户ID
     * @return 返回用户信息或错误
     */
    suspend fun getCurrentUser(): Result<User?, AppError>

    /**
     * 更新用户信息
     *
     * @param user 用户对象，包含要更新的信息
     * @return 成功时返回 true，否则返回 false
     */
    suspend fun updateUser(user: User): Boolean

    /**
     * 删除用户
     *
     * @param uid 用户ID
     * @return 成功时返回 true，否则返回 false
     */
    suspend fun deleteUser(uid: String): Boolean

    /**
     * 注销当前用户。
     *
     * @return 成功时返回 [Result.Success]，失败时返回 [Result.Error]。
     */
    suspend fun logout(): Result<Unit, AuthError>
}