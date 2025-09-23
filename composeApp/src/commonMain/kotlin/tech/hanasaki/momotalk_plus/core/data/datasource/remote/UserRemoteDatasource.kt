package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.GetSessionResponse
import tech.hanasaki.momotalk_plus.core.domain.model.UserError

class UserRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "http://localhost:3001/api"

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
     * 获取会话信息
     *
     * @return Result<GetAccountInfoResponse> 成功时返回会话信息响应，失败时返回错误信息
     */
    suspend fun getSessionInfo(): Result<GetSessionResponse?, UserError> =
        getRequest<GetSessionResponse>(
            "$endpoint/auth/get-session",
        )

    /**
     * 用户登出
     *
     * @return Result<Unit> 成功时返回空结果，失败时返回错误信息
     */
    suspend fun logout(): Result<Unit, UserError> =
        try {
            client.post("$endpoint/auth/sign-out")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(UserError.NetworkError(e))
        }


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