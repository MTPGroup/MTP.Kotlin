package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.BaseRemoteDataSource
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.GetSessionResponse

class UserRemoteDatasource(client: HttpClient) : BaseRemoteDataSource(client) {

    /**
     * 获取会话信息
     *
     * @return Result<GetAccountInfoResponse> 成功时返回会话信息响应，失败时返回错误信息
     */
    suspend fun getSessionInfo(): IResult<GetSessionResponse?, AppError> =
        get<GetSessionResponse>(
            "$baseUrl/auth/get-session",
        )

    /**
     * 用户登出
     *
     * @return Result<Unit> 成功时返回空结果，失败时返回错误信息
     */
    suspend fun logout(): IResult<Unit, AppError> =
        post("$baseUrl/auth/logout")
}