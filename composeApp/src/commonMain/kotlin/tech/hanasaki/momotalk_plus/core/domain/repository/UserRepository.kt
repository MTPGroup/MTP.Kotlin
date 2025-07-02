package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.UserError

interface UserRepository {
    /**
     * 刷新用户的 ID 令牌。
     *
     * @return 返回一个 [Result]，成功时包含新的 ID 令牌，失败时包含错误信息。
     */
    suspend fun refreshIdToken(): Result<RefreshInfo, UserError>

    /**
     * 获取当前用户信息
     *
     * @param idToken Firebase ID Token，用于验证用户身份
     * @return 返回用户信息或错误
     */
    suspend fun getCurrentUser(idToken: String): Result<UserProfile, UserError>

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
    fun logout(): Result<Unit, UserError>


    /**
     * 获取登录状态。
     *
     * @return 返回一个 [Flow]，包含当前用户的 uid，如果未登录则为 null。
     */
    fun getLoginState(): Flow<String?>

    /**
     * 保存登录状态。
     *
     * @param uid 用户的唯一标识符。
     * @param idToken 用户的 ID 令牌。
     * @param refreshToken 刷新令牌。
     * @param expiresIn 令牌的过期时间（秒）。
     * @return 成功时返回 [Result.Success]，失败时返回 [Result.Error]。
     */
    suspend fun saveLoginState(
        uid: String,
        idToken: String,
        refreshToken: String,
        expiresIn: Long,
    ): Result<Unit, AppError>
}