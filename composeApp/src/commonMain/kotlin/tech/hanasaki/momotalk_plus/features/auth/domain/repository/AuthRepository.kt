package tech.hanasaki.momotalk_plus.features.auth.domain.repository

import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.dto.SignInWithPasswordResponse
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
    )

    /**
     * 通过密码登录用户。
     *
     * @param email 用户的电子邮件地址。
     * @param password 用户的密码
     */
    suspend fun signInWithPassword(
        email: String,
        password: String,
    ): SignInWithPasswordResponse

    /**
     * 登出当前用户。
     *
     */
    suspend fun signOut()

    /**
     * 发送邮箱验证邮件。
     *
     * @param email 用户的电子邮件地址。
     * @param type 验证邮件的类型。
     */
    suspend fun sendEmailVerification(email: String, type: OTPType)

    /**
     * 发送密码重置邮件。
     *
     * @param email 用户的电子邮件地址。
     */
    suspend fun sendPasswordResetEmail(email: String)

    /**
     * 验证邮箱验证码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     */
    suspend fun verifyEmail(email: String, otp: String)

    /**
     * 重置用户密码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     * @param password 新密码。
     */
    suspend fun resetPassword(email: String, otp: String, password: String)
}