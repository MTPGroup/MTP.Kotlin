package tech.hanasaki.momotalk_plus.features.auth.domain.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.data.model.SignInWithPasswordResponse
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType

interface AuthRepository {
    /**
     * 注册新用户。
     *
     * @param email 用户的电子邮件地址。
     * @param username 用户的手机号码。
     * @param password 用户的密码。
     */
    suspend fun signUp(
        email: String,
        username: String,
        password: String,
    ): Result<Unit, AuthError>

    /**
     * 通过密码登录用户。
     *
     * @param email 用户的电子邮件地址。
     * @param password 用户的密码
     * @return 返回一个 [Result]，成功时包含用户的 ID 令牌，失败时包含 [AuthError]。
     */
    suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<SignInWithPasswordResponse, AuthError>

    /**
     * 登出当前用户。
     *
     * @return 返回一个 [Result]，成功时包含 `Unit`，失败时包含 [AuthError]。
     */
    suspend fun signOut(): Result<Unit, AuthError>

    /**
     * 发送邮箱验证邮件。
     *
     * @param email 用户的电子邮件地址。
     * @param type 验证邮件的类型。
     * @return 返回一个 [Result]，成功时包含 `Unit`，失败时包含 [AuthError]。
     */
    suspend fun sendEmailVerification(email: String, type: OTPType): Result<Unit, AuthError>

    /**
     * 发送密码重置邮件。
     *
     * @param email 用户的电子邮件地址。
     * @return 返回一个 [Result]，成功时包含 `Unit`，失败时包含 [AuthError]。
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, AuthError>

    /**
     * 验证邮箱验证码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     * @return 返回一个 [Result]，成功时包含 `Unit`，失败时包含 [AuthError]。
     */
    suspend fun verifyEmail(email: String, otp: String): Result<Unit, AuthError>

    /**
     * 重置用户密码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     * @param password 新密码。
     * @return 返回一个 [Result]，成功时包含 `Unit`，失败时包含 [AuthError]。
     */
    suspend fun resetPassword(email: String, otp: String, password: String): Result<Unit, AuthError>
}