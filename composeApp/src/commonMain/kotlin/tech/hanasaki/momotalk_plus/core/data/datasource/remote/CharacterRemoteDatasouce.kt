package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.BaseRemoteDatasource
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.*
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility

class CharacterRemoteDatasource(client: HttpClient) : BaseRemoteDatasource(client) {
    private val endpoint = "$baseUrl/characters"

    /**
     * 创建角色
     *
     * @param name 角色名称
     * @param persona 角色设定
     * @param signature 角色签名
     * @param avatarUrl 角色头像URL
     * @param visibility 角色可见性
     *
     * @return IResult<[CreateCharacterResponse], [AppError]>
     */
    suspend fun createCharacter(
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<CreateCharacterResponse, AppError> =
        post(
            endpoint,
            CreateCharacterRequest(
                name,
                signature,
                avatarUrl,
                persona,
                visibility,
            ),
        )

    /**
     * 删除角色
     *
     * @param id 角色ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun deleteCharacter(id: String): IResult<Unit, AppError> =
        delete(
            "$endpoint/$id",
        )

    /**
     * 查询角色详细信息
     *
     * @param id 角色ID
     * @return IResult<[CharacterDetailResponse], [AppError]>
     */
    suspend fun searchCharacterById(id: String): IResult<CharacterDetailResponse, AppError> =
        get<CharacterDetailResponse>(
            "$endpoint/$id",
        )

    /**
     * 查询可用角色列表
     *
     * @return IResult<[ListCharacterResponse], [AppError]>
     */
    suspend fun listCharacters(): IResult<ListCharacterResponse, AppError> =
        get<ListCharacterResponse>(
            endpoint,
            mapOf("visibility" to "all")
        )

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param name 角色名称
     * @param persona 角色设定
     * @param signature 角色签名
     * @param avatarUrl 角色头像URL
     * @param visibility 角色可见性
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun updateCharacter(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<Unit, AppError> =
        put<UpdateCharacterResponse>(
            "$endpoint/$id",
            UpdateCharacterRequest(
                name,
                persona,
                signature,
                avatarUrl,
                visibility
            ),
        ).map { }
}