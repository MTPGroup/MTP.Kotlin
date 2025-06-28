package tech.hanasaki.momotalk_plus.features.login.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.User
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError

interface AuthRepository {
    /**
     * 通过电子邮件和密码注册新用户。
     *
     * @param email 用户的电子邮件地址。
     * @param password 用户的密码。
     */
    suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Result<Unit, AuthError>

    /**
     * 通过电子邮件和密码登录用户。
     *
     * @param email 用户的电子邮件地址。
     * @param password 用户的密码
     * @return 返回一个 [Result]，成功时包含用户的 ID 令牌，失败时包含 [AuthError]。
     */
    suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<RefreshInfo, AuthError>

    /**
     * 发送电子邮件重置用户密码。
     *
     * @param email 用户的电子邮件地址。
     */
    suspend fun sendResetPasswordEmail(email: String): Result<Unit, AuthError>

    /**
     * 验证密码重置代码。
     *
     * @param oobCode 从电子邮件中获取的操作代码。
     */
    suspend fun verifyPasswordResetCode(oobCode: String): Result<Unit, AuthError>

    /**
     * 使用验证码确认新密码。
     *
     * @param oobCode 从电子邮件中获取的操作代码。
     * @param newPassword 用户的新密码。
     */
    suspend fun resetPassword(oobCode: String, newPassword: String): Result<Unit, AuthError>

    /**
     * 获取用户的认证状态流。
     *
     * @return 返回一个 [Flow]，流中的值为当前用户的 [User] 对象或 null（如果用户未登录）。
     */
    fun getAuthStateFlow(): Flow<User?>
}