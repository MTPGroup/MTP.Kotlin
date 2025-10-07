package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.utils.Constants
import tech.hanasaki.momotalk_plus.features.profile.data.model.UpdateUserRequest
import tech.hanasaki.momotalk_plus.features.profile.data.model.UpdateUserResponse

/**
 * ProfileRemoteDataSource - 个人资料远程数据源
 */
class ProfileRemoteDataSource(private val client: HttpClient) {
    private val endpoint = "${Constants.BASE_URL}/auth"

    /**
     * 通用POST请求处理方法
     */
    private suspend inline fun <reified R : Any> postRequest(
        url: String,
        requestBody: Any? = null,
        headers: Headers? = null,
    ): IResult<R, AppError> {
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
            IResult.Success(response)
        } catch (e: Exception) {
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }

    /**
     * 更新用户信息
     *
     * @param name 用户名
     * @param image 用户头像URL（可选）
     * @return IResult<UpdateUserResponse, AppError>
     */
    suspend fun updateUser(
        name: String,
        image: String?,
    ): IResult<UpdateUserResponse, AppError> =
        postRequest<UpdateUserResponse>(
            "$endpoint/update-user",
            UpdateUserRequest(name = name, image = image)
        )
}

