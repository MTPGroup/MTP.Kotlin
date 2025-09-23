package tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.data.model.*
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType


class AuthRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "http://127.0.0.1:3001/api/auth"

    private suspend inline fun <reified R : Any> postAuthRequest(
        url: String,
        requestBody: Any? = null,
        headers: Headers? = null,
    ): Result<R, AuthError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                if (requestBody != null) {
                    setBody(requestBody)
                }
                headers {
                    if (headers != null) {
                        appendAll(headers)
                    }
                }
            }.body()
            Result.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                Result.Error(AuthError.ApiError(-1, "发生错误: $errorBody"))
            } catch (_: Exception) {
                Result.Error(
                    AuthError.ApiError(
                        e.response.status.value,
                        "客户端请求失败，无法解析错误信息。",
                    )
                )
            }
        } catch (e: ServerResponseException) {
            Result.Error(
                AuthError.ApiError(
                    e.response.status.value,
                    "服务器响应错误: ${e.response.status.description}",
                )
            )
        } catch (e: RedirectResponseException) {
            Result.Error(
                AuthError.ApiError(
                    e.response.status.value,
                    "重定向错误: ${e.response.status.description}",
                )
            )
        } catch (e: Exception) {
            Result.Error(AuthError.NetworkError(e))
        }
    }

    /**
     * 使用电子邮件和密码注册新用户
     *
     * @param email 用户的电子邮件地址
     * @param name 用户名
     * @param password 用户的密码
     * @param callbackURL 回调 URL，可选
     * @return Result<SignUpResponse> 成功时返回注册响应，失败时返回错误信息
     */
    suspend fun signUpWithPassword(
        name: String,
        email: String,
        password: String,
        callbackURL: String = "",
    ): Result<SignUpResponse, AuthError> =
        postAuthRequest(
            "$endpoint/sign-up/email",
            SignUpRequest(email, name, password, callbackURL),
        )

    /**
     * 使用邮箱密码登录用户
     *
     * @param email 用户的电子邮件地址
     * @param password 用户的密码
     * @return Result<SignInWithPasswordResponse> 成功时返回登录响应，失败时返回错误信息
     */
    suspend fun signInWithPassword(
        email: String,
        password: String,
    ): Result<SignInWithPasswordResponse, AuthError> =
        postAuthRequest(
            "$endpoint/sign-in/email",
            SignInWithPasswordRequest(email, password),
        )

    /**
     * 登出当前用户
     *
     * @return Result<SignOutResponse> 成功时返回登出响应，失败时返回错误信息
     */
    suspend fun signOut(): Result<SignOutResponse, AuthError> =
        postAuthRequest(
            "$endpoint/sign-out",
            SignOutRequest,
        )

    /**
     * 发送邮箱验证邮件
     *
     * @param email 用户的电子邮件地址
     * @param type 验证邮件的类型
     * @return Result<SendEmailVerificationResponse> 成功时返回发送响应，失败时返回错误信息
     */
    suspend fun sendEmailVerification(
        email: String,
        type: OTPType,
    ): Result<SendEmailVerificationResponse, AuthError> =
        postAuthRequest(
            "$endpoint/email-otp/send-verification-otp",
            SendEmailVerificationRequest(email, type),
        )

    /**
     * 发送密码重置邮件
     *
     * @param email 用户的电子邮件地址
     * @return Result<SendPasswordResetEmailResponse> 成功时返回发送响应，失败时返回错误信息
     */
    suspend fun sendPasswordResetEmail(
        email: String,
    ): Result<SendPasswordResetEmailResponse, AuthError> =
        postAuthRequest(
            "$endpoint/forget-password/email-otp",
            SendPasswordResetEmailRequest(email),
        )

    /**
     * 验证邮箱验证码
     *
     * @param email 用户的电子邮件地址
     * @param otp 验证码
     * @return Result<VerifyOTPResponse> 成功时返回验证响应，失败时返回错误信息
     */
    suspend fun verifyEmail(
        email: String,
        otp: String,
    ): Result<VerifyOTPResponse, AuthError> =
        postAuthRequest(
            "$endpoint/email-otp/verify-email",
            VerifyOTPRequest(email, otp),
        )

    /**
     * 重置用户密码
     *
     * @param email 用户的电子邮件地址
     * @param otp 验证码
     * @param password 新密码
     * @return Result<ResetPasswordResponse> 成功时返回重置响应，失败时返回错误信息
     */
    suspend fun resetPassword(
        email: String,
        otp: String,
        password: String,
    ): Result<ResetPasswordResponse, AuthError> =
        postAuthRequest(
            "$endpoint/email-otp/reset-password",
            ResetPasswordRequest(email, otp, password),
        )
}

