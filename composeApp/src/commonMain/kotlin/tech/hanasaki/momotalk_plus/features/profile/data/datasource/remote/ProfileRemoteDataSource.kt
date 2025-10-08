package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote

import io.ktor.client.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.BaseRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.profile.data.model.UpdateUserRequest
import tech.hanasaki.momotalk_plus.features.profile.data.model.UpdateUserResponse

/**
 * ProfileRemoteDataSource - 个人资料远程数据源
 */
class ProfileRemoteDataSource(client: HttpClient) : BaseRemoteDatasource(client) {
    private val endpoint = "$baseUrl/auth"


    /**
     * 更新用户信息
     *
     * @param name 用户名
     * @param image 用户头像URL（可选）
     *
     * @return IResult<[UpdateUserResponse], [AppError]>
     */
    suspend fun updateUser(
        name: String,
        image: String?,
    ): IResult<UpdateUserResponse, AppError> {
        return post<UpdateUserResponse>(
            "$endpoint/update-user",
            UpdateUserRequest(name = name, image = image),
        )
    }
}
