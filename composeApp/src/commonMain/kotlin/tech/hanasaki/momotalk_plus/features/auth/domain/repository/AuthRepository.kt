package tech.hanasaki.momotalk_plus.features.auth.domain.repository

import io.github.jan.supabase.auth.OtpType

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
    )

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
    suspend fun sendEmailVerification(email: String, type: OtpType.Email)

    /**
     * 验证邮箱验证码。
     *
     * @param type 验证邮件的类型。
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     */
    suspend fun verifyEmail(type: OtpType.Email, email: String, otp: String)

    /**
     *
     */
    suspend fun sendResetPasswordEmail(email: String)

    /**
     * 重置用户密码。
     *
     * @param email 用户的电子邮件地址。
     * @param password 新密码。
     */
    suspend fun resetPassword(email: String, password: String)
}