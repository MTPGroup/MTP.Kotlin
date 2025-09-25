package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.*
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

class CharacterRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "http://localhost:3001/api/character"

    private suspend inline fun <reified T : Any, reified R : Any> postRequest(
        url: String,
        requestBody: T,
    ): IResult<R, UserError> {
        return try {
            val response: R = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                IResult.Error(
                    UserError.ApiError(
                        -1,
                        ""
                    )
                )
            } catch (parseEx: Exception) {
                IResult.Error(
                    UserError.ApiError(
                        e.response.status.value,
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(UserError.NetworkError(e))
        }
    }

    private suspend inline fun <reified T : Any> getRequest(
        url: String,
    ): IResult<T, UserError> {
        return try {
            val response: T = client.get(url).body()
            println("收到响应: $response")
            IResult.Success(response)
        } catch (e: ClientRequestException) {
            try {
                val errorBody = e.response.body<Any>()
                println(errorBody)
                IResult.Error(
                    UserError.ApiError(
                        -1,
                        ""
                    )
                )
            } catch (parseEx: Exception) {
                IResult.Error(
                    UserError.ApiError(
                        e.response.status.value,
                        "客户端请求失败，无法解析错误信息。"
                    )
                )
            }
        } catch (e: ServerResponseException) {
            IResult.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "服务器错误: ${e.response.status}"
                )
            )
        } catch (e: RedirectResponseException) {
            IResult.Error(
                UserError.ApiError(
                    e.response.status.value,
                    "重定向错误: ${e.response.status}"
                )
            )
        } catch (e: Exception) {
            IResult.Error(UserError.NetworkError(e))
        }
    }

    /**
     * 创建角色
     *
     * @param name 角色名称
     * @param creatorId 创建者ID
     */
    suspend fun createCharacter(
        name: String,
        creatorId: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<CreateCharacterResponse, UserError> =
        postRequest<CreateCharacterRequest, CreateCharacterResponse>(
            "$endpoint/create",
            CreateCharacterRequest(
                name,
                creatorId,
                persona,
                signature,
                avatarUrl,
                visibility,
            )
        )

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return Result<Unit, UserError>
     */
    suspend fun deleteCharacter(id: String): IResult<Unit, UserError> {
        try {
            client.delete("$endpoint/delete/$id")
            return IResult.Success(Unit)
        } catch (e: Exception) {
            return IResult.Error(UserError.NetworkError(e))
        }
    }

    /**
     * 查询角色详细信息
     *
     * @param id 角色ID
     * @return Result<CharacterDetailResponse, UserError>
     */
    suspend fun searchCharacterById(id: String): IResult<CharacterDetailResponse, UserError> =
        getRequest<CharacterDetailResponse>("$endpoint/$id")

    /**
     * 查询可用角色列表
     *
     * @return Result<ListCharacterResponse, UserError>
     */
    suspend fun listCharacters(): IResult<ListCharacterResponse, UserError> =
        getRequest<ListCharacterResponse>("$endpoint/list")

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param name 角色名称
     * @param persona 角色设定
     * @param signature 角色签名
     * @param avatarUrl 角色头像URL
     * @param visibility 角色可见性
     * @return Result<Unit, UserError>
     */
    suspend fun updateCharacter(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<Unit, UserError> =
        postRequest<UpdateCharacterRequest, UpdateCharacterResponse>(
            "$endpoint/update/$id",
            UpdateCharacterRequest(
                name,
                persona,
                signature,
                avatarUrl,
                visibility,
            )
        ).let { result ->
            when (result) {
                is IResult.Success -> IResult.Success(Unit)
                is IResult.Error -> IResult.Error(result.error)
            }
        }
}