package tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote

import io.ktor.client.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.BaseRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.auth.data.model.*
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType


class AuthRemoteDatasource(client: HttpClient) : BaseRemoteDatasource(client) {
    private val endpoint = "$baseUrl/auth"


    /**
     * 使用电子邮件和密码注册新用户
     *
     * @param email 用户的电子邮件地址
     * @param name 用户名
     * @param password 用户的密码
     * @param callbackURL 回调 URL，可选
     *
     * @return IResult<[SignUpResponse], [AppError]> 成功时返回注册响应，失败时返回错误信息
     */
    suspend fun signUpWithPassword(
        name: String,
        email: String,
        password: String,
        callbackURL: String = "",
    ): IResult<SignUpResponse, AppError> =
        post(
            "$endpoint/sign-up/email",
            SignUpRequest(email, name, password, callbackURL),
        )

    /**
     * 使用邮箱密码登录用户
     *
     * @param email 用户的电子邮件地址
     * @param password 用户的密码
     *
     * @return IResult<[SignInWithPasswordResponse], [AppError]> 成功时返回登录响应，失败时返回错误信息
     */
    suspend fun signInWithPassword(
        email: String,
        password: String,
    ): IResult<SignInWithPasswordResponse, AppError> =
        post(
            "$endpoint/sign-in/email",
            SignInWithPasswordRequest(email, password),
        )

    /**
     * 登出当前用户
     *
     * @return IResult<[SignOutResponse], [AppError]> 成功时返回登出响应，失败时返回错误信息
     */
    suspend fun signOut(): IResult<SignOutResponse, AppError> =
        post(
            "$endpoint/sign-out",
            SignOutRequest,
        )

    /**
     * 发送邮箱验证邮件
     *
     * @param email 用户的电子邮件地址
     * @param type 验证邮件的类型
     *
     * @return IResult<[SendEmailVerificationResponse], [AppError]> 成功时返回发送响应，失败时返回错误信息
     */
    suspend fun sendEmailVerification(
        email: String,
        type: OTPType,
    ): IResult<SendEmailVerificationResponse, AppError> =
        post(
            "$endpoint/email-otp/send-verification-otp",
            SendEmailVerificationRequest(email, type),
        )

    /**
     * 发送密码重置邮件
     *
     * @param email 用户的电子邮件地址
     *
     * @return IResult<[SendPasswordResetEmailResponse], [AppError]> 成功时返回发送响应，失败时返回错误信息
     */
    suspend fun sendPasswordResetEmail(
        email: String,
    ): IResult<SendPasswordResetEmailResponse, AppError> =
        post(
            "$endpoint/forget-password/email-otp",
            SendPasswordResetEmailRequest(email),
        )

    /**
     * 验证邮箱验证码
     *
     * @param email 用户的电子邮件地址
     * @param otp 验证码
     * @return IResult<[VerifyOTPResponse], [AppError]> 成功时返回验证响应，失败时返回错误信息
     */
    suspend fun verifyEmail(
        email: String,
        otp: String,
    ): IResult<VerifyOTPResponse, AppError> =
        post(
            "$endpoint/email-otp/verify-email",
            VerifyOTPRequest(email, otp),
        )

    /**
     * 重置用户密码
     *
     * @param email 用户的电子邮件地址
     * @param otp 验证码
     * @param password 新密码
     *
     * @return IResult<[ResetPasswordResponse], [AppError]> 成功时返回重置响应，失败时返回错误信息
     */
    suspend fun resetPassword(
        email: String,
        otp: String,
        password: String,
    ): IResult<ResetPasswordResponse, AppError> =
        post(
            "$endpoint/email-otp/reset-password",
            ResetPasswordRequest(email, otp, password),
        )
}

