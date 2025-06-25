package tech.hanasaki.momotalk_plus.features.login.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import tech.hanasaki.momotalk_plus.features.login.data.model.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.FirebaseErrorResponse
import tech.hanasaki.momotalk_plus.config.BuildKonfig
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError


class AuthRemoteDatasource(private val client: HttpClient) {
    private val apiKey = BuildKonfig.firebaseApiKey
    private val endpoint = "https://identitytoolkit.googleapis.com/v1/accounts"

    private suspend inline fun <reified T : Any, reified R : Any> postAuthRequest(
        url: String,
        requestBody: T
    ): Result<R, AuthError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            Result.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<FirebaseErrorResponse>()
                Result.Error(AuthError.ApiError(errorBody.error.code, errorBody.error.message))
            } catch (parseEx: Exception) {
                Result.Error(AuthError.ApiError(e.response.status.value, "Client request failed, could not parse error."))
            }
        } catch (e: ServerResponseException) {
            Result.Error(AuthError.ApiError(e.response.status.value, "Server error: ${e.response.status}"))
        } catch (e: RedirectResponseException) {
            Result.Error(AuthError.ApiError(e.response.status.value, "Redirect error: ${e.response.status}"))
        } catch (e: Exception) {
            Result.Error(AuthError.NetworkError(e))
        }
    }

    /**
     * 使用电子邮件和密码注册新用户
     *
     * @param email 用户的电子邮件地址
     * @param password 用户的密码
     * @return Result<SignUpResponse> 成功时返回注册响应，失败时返回错误信息
     */
    suspend fun signUpWithPassword(
        email: String,
        password: String,
    ): Result<SignUpResponse, AuthError> =
        postAuthRequest(
            "$endpoint:signUp?key=$apiKey",
            SignUpRequest(email, password),
        )

    /**
     * 使用电子邮件和密码登录用户
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
            "$endpoint:signInWithPassword?key=$apiKey",
            SignInWithPasswordRequest(email, password),
        )

    /**
     * 发送重置密码邮件
     *
     * @param email 用户的电子邮件地址
     * @return Result<SendResetPasswordEmailResponse> 成功时返回发送邮件响应，失败时返回错误信息
     */
    suspend fun sendResetPasswordEmail(
        email: String,
    ): Result<SendResetPasswordEmailResponse, AuthError> =
        postAuthRequest(
            "$endpoint:sendOobCode?key=$apiKey",
            SendResetPasswordEmailRequest(email),
        )

    /**
     * 验证重置密码代码
     *
     * @param oobCode 重置密码的操作码
     * @return Result<VerifyResetPasswordCodeResponse> 成功时返回验证响应，失败时返回错误信息
     */
    suspend fun verifyResetPasswordCode(
        oobCode: String,
    ): Result<VerifyResetPasswordCodeResponse, AuthError> =
        postAuthRequest(
            "$endpoint:resetPassword?key=$apiKey",
            VerifyResetPasswordCodeRequest(oobCode),
        )

    /**
     * 重置密码
     *
     * @param oobCode 重置密码的操作码
     * @param newPassword 新密码
     * @return Result<ResetPasswordResponse> 成功时返回重置密码响应，失败时返回错误信息
     */
    suspend fun resetPassword(
        oobCode: String,
        newPassword: String,
    ): Result<ResetPasswordResponse, AuthError> =
        postAuthRequest(
            "$endpoint:resetPassword?key=$apiKey",
            ResetPasswordRequest(oobCode, newPassword),
        )

    /**
     * 更改用户电子邮件地址
     *
     * @param idToken 用户的身份验证令牌
     * @param email 新的电子邮件地址
     * @return Result<ChangeEmailResponse> 成功时返回更改电子邮件响应，失败时返回错误信息
     */
    suspend fun changeEmail(
        idToken: String,
        email: String,
    ): Result<ChangeEmailResponse, AuthError> =
        postAuthRequest(
            "$endpoint:update?key=$apiKey",
            ChangeEmailRequest(idToken, email),
        )


    /**
     * 发送电子邮件验证
     *
     * @param idToken 用户的身份验证令牌
     * @return Result<SendEmailVerificationResponse> 成功时返回发送验证邮件响应，失败时返回错误信息
     */
    suspend fun sendEmailVerification(
        idToken: String,
    ): Result<SendEmailVerificationResponse, AuthError> =
        postAuthRequest(
            "$endpoint:sendOobCode?key=$apiKey",
            SendEmailVerificationRequest(idToken),
        )

    /**
     * 验证电子邮件地址
     *
     * @param oobCode 验证操作码
     * @return Result<ConfirmEmailVerificationResponse> 成功时返回验证响应，失败时返回错误信息
     */
    suspend fun confirmEmailVerification(
        oobCode: String,
    ): Result<ConfirmEmailVerificationResponse, AuthError> =
        postAuthRequest(
            "$endpoint:resetPassword?key=$apiKey",
            ConfirmEmailVerificationRequest(oobCode),
        )

}