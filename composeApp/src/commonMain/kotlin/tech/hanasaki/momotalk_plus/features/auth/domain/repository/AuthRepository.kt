package tech.hanasaki.momotalk_plus.features.auth.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.data.model.SignInWithPasswordResponse
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
    ): IResult<Unit, AppError>

    /**
     * 通过密码登录用户。
     *
     * @param email 用户的电子邮件地址。
     * @param password 用户的密码
     * @return 返回一个 [IResult]，成功时包含用户的 ID 令牌，失败时包含 [AppError]。
     */
    suspend fun signInWithPassword(
        email: String,
        password: String,
    ): IResult<SignInWithPasswordResponse, AppError>

    /**
     * 登出当前用户。
     *
     * @return 返回一个 [IResult]，成功时包含 `Unit`，失败时包含 [AppError]。
     */
    suspend fun signOut(): IResult<Unit, AppError>

    /**
     * 发送邮箱验证邮件。
     *
     * @param email 用户的电子邮件地址。
     * @param type 验证邮件的类型。
     * @return 返回一个 [IResult]，成功时包含 `Unit`，失败时包含 [AppError]。
     */
    suspend fun sendEmailVerification(email: String, type: OTPType): IResult<Unit, AppError>

    /**
     * 发送密码重置邮件。
     *
     * @param email 用户的电子邮件地址。
     * @return 返回一个 [IResult]，成功时包含 `Unit`，失败时包含 [AppError]。
     */
    suspend fun sendPasswordResetEmail(email: String): IResult<Unit, AppError>

    /**
     * 验证邮箱验证码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     * @return 返回一个 [IResult]，成功时包含 `Unit`，失败时包含 [AppError]。
     */
    suspend fun verifyEmail(email: String, otp: String): IResult<Unit, AppError>

    /**
     * 重置用户密码。
     *
     * @param email 用户的电子邮件地址。
     * @param otp 验证码。
     * @param password 新密码。
     * @return 返回一个 [IResult]，成功时包含 `Unit`，失败时包含 [AppError]。
     */
    suspend fun resetPassword(email: String, otp: String, password: String): IResult<Unit, AppError>
}