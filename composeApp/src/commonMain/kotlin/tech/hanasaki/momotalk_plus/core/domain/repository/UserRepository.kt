package tech.hanasaki.momotalk_plus.core.domain.repository

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.User

interface UserRepository {
    /**
     * 获取当前用户信息
     *
     * @return 返回用户信息或错误
     */
    suspend fun getCurrentUser(): IResult<UserProfile?, AppError>

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
     * @return 成功时返回 [IResult.Success]，失败时返回 [IResult.Error]。
     */
    suspend fun logout(): IResult<Unit, AppError>


    /**
     * 获取登录状态。
     *
     * @return 返回当前的登录状态，true 表示已登录，false 表示未登录。
     */
    suspend fun getLoginState(): Boolean
}