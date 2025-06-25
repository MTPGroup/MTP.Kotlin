package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import tech.hanasaki.momotalk_plus.config.BuildKonfig
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.*
import tech.hanasaki.momotalk_plus.features.login.data.model.ChangePasswordRequest
import tech.hanasaki.momotalk_plus.features.login.data.model.ChangePasswordResponse
import tech.hanasaki.momotalk_plus.features.login.data.model.DeleteAccountRequest
import tech.hanasaki.momotalk_plus.features.login.data.model.LinkEmailAndPasswordRequest
import tech.hanasaki.momotalk_plus.features.login.data.model.LinkEmailAndPasswordResponse

sealed class UserDatasourceError {
    data class ApiError(val code: Int, val message: String) : UserDatasourceError()
    data class NetworkError(val originalException: Exception) : UserDatasourceError()
    data object Unknown : UserDatasourceError()
}

class UserRemoteDatasource(private val client: HttpClient) {
    private val apiKey = BuildKonfig.firebaseApiKey
    private val endpoint = "https://identitytoolkit.googleapis.com/v1/accounts"

    private suspend inline fun <reified T : Any, reified R : Any> postAuthRequest(
        url: String,
        requestBody: T
    ): Result<R, UserDatasourceError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            Result.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<FirebaseErrorResponse>()
                Result.Error(
                    UserDatasourceError.ApiError(
                        errorBody.error.code,
                        errorBody.error.message
                    )
                )
            } catch (parseEx: Exception) {
                Result.Error(
                    UserDatasourceError.ApiError(
                        e.response.status.value,
                        "Client request failed, could not parse error."
                    )
                )
            }
        } catch (e: ServerResponseException) {
            Result.Error(
                UserDatasourceError.ApiError(
                    e.response.status.value,
                    "Server error: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            Result.Error(
                UserDatasourceError.ApiError(
                    e.response.status.value,
                    "Redirect error: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            Result.Error(UserDatasourceError.NetworkError(e))
        }
    }

    /**
     * 设置用户账户信息
     *
     * @param idToken 用户的身份验证令牌
     * @param displayName 用户的显示名称
     * @param photoUrl 用户的头像URL
     * @return Result<SetAccountInfoResponse> 成功时返回设置账户信息响应，失败时返回错误信息
     */
    suspend fun setAccountInfo(
        idToken: String,
        displayName: String? = null,
        photoUrl: String? = null,
    ): Result<SetAccountInfoResponse, UserDatasourceError> =
        postAuthRequest(
            "$endpoint:update?key=$apiKey",
            SetAccountInfoRequest(idToken, displayName, photoUrl),
        )

    /**
     * 获取用户账户信息
     *
     * @param idToken 用户的身份验证令牌
     * @return Result<GetAccountInfoResponse> 成功时返回账户信息响应，失败时返回错误信息
     */
    suspend fun getAccountInfo(
        idToken: String,
    ): Result<GetAccountInfoResponse, UserDatasourceError> =
        postAuthRequest(
            "$endpoint:lookup?key=$apiKey",
            GetAccountInfoRequest(idToken),
        )

    /**
     * 更改用户密码
     *
     * @param idToken 用户的身份验证令牌
     * @param password 新密码
     * @return Result<ChangePasswordResponse> 成功时返回更改密码响应，失败时返回错误信息
     */
    suspend fun changePassword(
        idToken: String,
        password: String,
    ): Result<ChangePasswordResponse, UserDatasourceError> =
        postAuthRequest(
            "$endpoint:update?key=$apiKey",
            ChangePasswordRequest(idToken, password),
        )


    /**
     * 链接电子邮件和密码到现有用户账户
     *
     * @param idToken 用户的身份验证令牌
     * @param email 用户的电子邮件地址
     * @param password 用户的密码
     * @return Result<LinkEmailAndPasswordResponse> 成功时返回链接响应，失败时返回错误信息
     */
    suspend fun linkEmailAndPassword(
        idToken: String,
        email: String,
        password: String,
    ): Result<LinkEmailAndPasswordResponse, UserDatasourceError> =
        postAuthRequest(
            "$endpoint:link?key=$apiKey",
            LinkEmailAndPasswordRequest(idToken, email, password),
        )

    /**
     * 删除用户账户
     *
     * @param idToken 用户的身份验证令牌
     * @return Result<Unit> 成功时返回空结果，失败时返回错误信息
     */
    suspend fun deleteAccount(
        idToken: String,
    ): Result<Unit, UserDatasourceError> =
        postAuthRequest(
            "$endpoint:delete?key=$apiKey",
            DeleteAccountRequest(idToken),
        )
}