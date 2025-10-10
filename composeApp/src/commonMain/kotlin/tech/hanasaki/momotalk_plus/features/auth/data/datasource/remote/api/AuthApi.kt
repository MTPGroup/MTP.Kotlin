package tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.dto.*

interface AuthApi {
    /**
     * 使用电子邮件和密码注册新用户
     */
    @Headers("Content-Type: application/json")
    @POST("auth/sign-up/email")
    suspend fun signUp(
        @Body request: SignUpRequest,
    ): SignUpResponse

    /**
     * 使用邮箱密码登录用户
     */
    @Headers("Content-Type: application/json")
    @POST("auth/sign-in/email")
    suspend fun signIn(
        @Body request: SignInWithPasswordRequest,
    ): SignInWithPasswordResponse

    /**
     * 登出当前用户
     */
    @POST("auth/sign-out")
    suspend fun signOut()

    /**
     * 发送邮箱验证邮件
     */
    @Headers("Content-Type: application/json")
    @POST("auth/email-otp/send-verification-otp")
    suspend fun sendEmailVerification(
        @Body request: SendEmailVerificationRequest,
    ): SendEmailVerificationResponse

    /**
     * 发送密码重置邮件
     */
    @Headers("Content-Type: application/json")
    @POST("auth/forget-password/email-otp")
    suspend fun sendForgetPasswordEmail(
        @Body request: SendPasswordResetEmailRequest,
    ): SendPasswordResetEmailResponse

    /**
     * 验证邮箱验证码
     */
    @Headers("Content-Type: application/json")
    @POST("auth/email-otp/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyOTPRequest,
    )

    /**
     * 重置密码
     */
    @Headers("Content-Type: application/json")
    @POST("auth/email-otp/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest,
    )
}