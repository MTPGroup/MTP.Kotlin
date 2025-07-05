package tech.hanasaki.momotalk_plus.features.login.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.data.model.*
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError


class AuthRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "https://cloud1-4gdmg8xt1b179a1c.api.tcloudbasegateway.com/auth/v1"

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
                Result.Error(AuthError.ApiError(-1, "Error occurred: ${errorBody.toString()}"))
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
     * @param username 用户名
     * @param password 用户的密码
     * @param verificationToken 验证令牌
     * @return Result<SignUpResponse> 成功时返回注册响应，失败时返回错误信息
     */
    suspend fun signUpWithPassword(
        email: String? = null,
        phoneNumber: String? = null,
        username: String,
        password: String,
        verificationToken: String
    ): Result<SignUpResponse, AuthError> =
        postAuthRequest(
            "$endpoint/signup",
            SignUpRequest(email, phoneNumber, username, password, verificationToken),
        )

    /**
     * 使用密码登录用户
     *
     * @param password 用户的密码
     * @return Result<SignInWithPasswordResponse> 成功时返回登录响应，失败时返回错误信息
     */
    suspend fun signInWithPassword(
        username: String,
        password: String,
    ): Result<SignInWithPasswordResponse, AuthError> =
        postAuthRequest(
            "$endpoint/signin",
            SignInWithPasswordRequest(username, password),
        )

    /**
     * 发送电子邮件或短信验证码
     *
     * @param email 用户的电子邮件地址（可选）
     * @param phoneNumber 用户的手机号码（可选）
     * @param target 验证目标，默认为 "ANY"
     */
    suspend fun sendVerificationCode(
        email: String? = null,
        phoneNumber: String? = null,
        captchaId: String,
        target: String = "ANY",
    ): Result<String, AuthError> =
        postAuthRequest<SendVerificationCodeResponse>(
            "$endpoint/verification",
            SendVerificationCodeRequest(email, phoneNumber, target),
            headers = headers { append("x-captcha-token", captchaId) }
        ).map { it.verificationId }

    /**
     * 获取验证令牌
     *
     * @param verificationId 从发送验证码的响应中获取的验证 ID
     * @param verificationCode 用户输入的验证码
     *
     * @return Result<String, AuthError> 成功时返回验证令牌，失败时返回错误信息
     */
    suspend fun getVerificationToken(
        verificationId: String,
        verificationCode: String,
    ): Result<String, AuthError> =
        postAuthRequest<VerifyCodeResponse>(
            "$endpoint/verification/verify",
            VerifyCodeRequest(
                verificationId = verificationId,
                verificationCode = verificationCode,
            )
        ).map { it.verificationToken }

    /**
     * 获取图片验证码
     *
     * @return Result<CaptchaResponse, AuthError> 成功时返回图片验证码响应，失败时返回错误信息
     */
    suspend fun getImageCaptcha(): Result<CaptchaResponse, AuthError> =
        postAuthRequest("$endpoint/captcha/data")

    /**
     * 获取图片验证码的验证令牌
     *
     * @param token 图片验证码的令牌
     * @param key 图片验证码的密钥
     *
     * @return Result<CaptchaIdResponse, AuthError> 成功时返回验证码 ID 响应，失败时返回错误信息
     */
    suspend fun getCaptchaId(token: String, key: String): Result<CaptchaIdResponse, AuthError> =
        postAuthRequest(
            "$endpoint/captcha/data/verify",
            CaptchaIdRequest(token, key)
        )

    /**
     * 重置用户密码
     *
     * @param email 用户的电子邮件地址（可选）
     * @param phoneNumber 用户的手机号码（可选）
     * @param newPassword 用户的新密码
     * @param verificationToken 验证令牌，用于验证用户身份
     *
     * @return Result<Unit, AuthError> 成功时返回 Unit，失败时返回错误信息
     */
    suspend fun resetPassword(
        email: String?,
        phoneNumber: String?,
        newPassword: String,
        verificationToken: String,
    ): Result<Unit, AuthError> =
        postAuthRequest<Any>(
            "$endpoint/reset",
            ResetPasswordRequest(
                email = email,
                phoneNumber = phoneNumber,
                newPassword = newPassword,
                verificationToken = verificationToken,
            )
        ).map { }


    // TODO: 添加手机号码注册/登录支持
}

