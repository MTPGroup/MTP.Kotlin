package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.UserError

class UserRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "http://localhost:3001/api"

    private suspend inline fun <reified T : Any, reified R : Any> postRequest(
        url: String,
        requestBody: T,
    ): Result<R, UserError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            Result.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                Result.Error(
                    UserError.ApiError(
                        -1,
                        ""
                    )
                )
            } catch (parseEx: Exception) {
                Result.Error(
                    UserError.ApiError(
                        e.response.status.value,
                        "Client request failed, could not parse error."
                    )
                )
            }
        } catch (e: ServerResponseException) {
            Result.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "Server error: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            Result.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "Redirect error: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            Result.Error(UserError.NetworkError(e))
        }
    }

    private suspend inline fun <reified T : Any> getRequest(
        url: String,
    ): Result<T, UserError> {
        return try {
            val response: T = client.get(url).body()
            println("收到响应: $response")
            Result.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                println(errorBody)
                Result.Error(
                    UserError.ApiError(
                        -1,
                        ""
                    )
                )
            } catch (parseEx: Exception) {
                Result.Error(
                    UserError.ApiError(
                        e.response.status.value,
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            Result.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            Result.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            Result.Error(UserError.NetworkError(e))
        }
    }

    /**
     * 清除用户令牌缓存
     */
    suspend fun clearToken(): Unit {
        client.authProviders.filterIsInstance<BearerAuthProvider>().firstOrNull()?.clearToken()
    }

    /**
     * 设置用户账户信息
     *
     * @param idToken 用户的身份验证令牌
     * @param displayName 用户的显示名称
     * @param photoUrl 用户的头像URL
     * @return Result<SetAccountInfoResponse> 成功时返回设置账户信息响应，失败时返回错误信息
     */
    /*suspend fun setAccountInfo(
        idToken: String,
        displayName: String? = null,
        photoUrl: String? = null,
    ): Result<SetAccountInfoResponse, UserError> =
        postRequest(
            "$endpoint:update?key=$apiKey",
            SetAccountInfoRequest(idToken, displayName, photoUrl),
        )*/


    /**
     * 获取用户账户信息
     *
     * @param idToken 用户的身份验证令牌
     * @return Result<GetAccountInfoResponse> 成功时返回账户信息响应，失败时返回错误信息
     */
    suspend fun getUserInfo(
        idToken: String,
    ): Result<UserProfile, UserError> =
        getRequest<UserProfile>(
            "$endpoint/user/me",
        )


    /**
     * 更改用户密码
     *
     * @param idToken 用户的身份验证令牌
     * @param password 新密码
     * @return Result<ChangePasswordResponse> 成功时返回更改密码响应，失败时返回错误信息
     */
    /*suspend fun changePassword(
        idToken: String,
        password: String,
    ): Result<ChangePasswordResponse, UserError> =
        postRequest(
            "$endpoint:update?key=$apiKey",
            ChangePasswordRequest(idToken, password),
        )*/


    /**
     * 链接电子邮件和密码到现有用户账户
     *
     * @param idToken 用户的身份验证令牌
     * @param email 用户的电子邮件地址
     * @param password 用户的密码
     * @return Result<LinkEmailAndPasswordResponse> 成功时返回链接响应，失败时返回错误信息
     */
    /*suspend fun linkEmailAndPassword(
        idToken: String,
        email: String,
        password: String,
    ): Result<LinkEmailAndPasswordResponse, UserError> =
        postRequest(
            "$endpoint:link?key=$apiKey",
            LinkEmailAndPasswordRequest(idToken, email, password),
        )*/


    /**
     * 删除用户账户
     *
     * @param idToken 用户的身份验证令牌
     * @return Result<Unit> 成功时返回空结果，失败时返回错误信息
     */
    /* suspend fun deleteAccount(
         idToken: String,
     ): Result<Unit, UserError> =
         postRequest(
             "$endpoint:delete?key=$apiKey",
             DeleteAccountRequest(idToken),
         )*/
}