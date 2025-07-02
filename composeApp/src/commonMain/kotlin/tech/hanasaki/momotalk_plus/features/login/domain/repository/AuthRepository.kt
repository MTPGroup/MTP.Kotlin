package tech.hanasaki.momotalk_plus.features.login.domain.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.data.model.CaptchaIdResponse
import tech.hanasaki.momotalk_plus.features.login.data.model.CaptchaResponse
import tech.hanasaki.momotalk_plus.features.login.data.model.SignInWithPasswordResponse
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError

interface AuthRepository {
    /**
     * 注册新用户。
     *
     * @param email 用户的电子邮件地址（可选）。
     * @param phoneNumber 用户的手机号码（可选）。
     * @param password 用户的密码。
     */
    suspend fun signUp(
        email: String?,
        phoneNumber: String?,
        username: String,
        password: String,
        verificationToken: String,
    ): Result<Unit, AuthError>

    /**
     * 通过密码登录用户。
     *
     * @param username 用户的电子邮件地址。
     * @param password 用户的密码
     * @return 返回一个 [Result]，成功时包含用户的 ID 令牌，失败时包含 [AuthError]。
     */
    suspend fun signInWithPassword(
        username: String,
        password: String
    ): Result<SignInWithPasswordResponse, AuthError>

    /**
     * 发送电子邮件/短信重置用户密码。
     *
     * @param email 用户的电子邮件地址（可选）。
     * @param phoneNumber 用户的手机号码（可选）。
     * @return 返回一个 [Result]，成功时包含 验证码id，失败时包含 [AuthError]。
     */
    suspend fun sendResetPasswordCode(
        email: String?,
        phoneNumber: String?,
        captchaId: String,
    ): Result<String, AuthError>

    /**
     * 验证密码重置代码。
     *
     * @param verificationId 从发送验证码的响应中获取的验证 ID。
     * @param verificationCode 用户输入的验证码。
     *
     * @return 返回一个 [Result]，成功时包含验证 token，失败时包含 [AuthError]。
     */
    suspend fun verifyPasswordResetCode(
        verificationId: String,
        verificationCode: String
    ): Result<String, AuthError>

    /**
     * 设置新密码。
     *
     * @param email 用户的电子邮件地址（可选）。
     * @param phoneNumber 用户的手机号码（可选）。
     * @param newPassword 用户的新密码。
     * @param verificationToken 验证 token，用于验证用户身份。
     *
     * @return 返回一个 [Result]，成功时包含 Unit，失败时包含 [AuthError]。
     */
    suspend fun resetPassword(
        email: String?,
        phoneNumber: String?,
        newPassword: String,
        verificationToken: String,
    ): Result<Unit, AuthError>

    /**
     * 获取图形验证码。
     *
     * @return 返回一个 [Result]，成功时包含图形验证码的 URL，失败时包含 [AuthError]。
     */
    suspend fun getImageCaptcha(): Result<CaptchaResponse, AuthError>

    /**
     * 验证图形验证码。
     *
     * @param captchaToken 图形验证码的令牌。
     * @param captchaInput 用户输入的验证码内容。
     *
     * @return 返回一个 [Result]，成功时包含验证码 ID 响应，失败时包含 [AuthError]。
     */
    suspend fun verifyImageCaptcha(
        captchaToken: String,
        captchaInput: String
    ): Result<CaptchaIdResponse, AuthError>
}