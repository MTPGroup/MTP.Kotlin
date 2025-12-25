package tech.hanasaki.momotalk_plus.core.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.*

/**
 * Character API - 使用 Ktorfit 定义的类型安全接口
 */
interface CharacterApi {

    /**
     * 获取角色列表
     */
    @GET("characters")
    suspend fun getCharacters(
        @Query("visibility") visibility: String = "all",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): CharacterListResponse

    /**
     * 根据ID获取角色详情
     */
    @GET("characters/{id}")
    suspend fun getCharacterById(
        @Path("id") id: String,
    ): CharacterDetailResponse

    /**
     * 创建新角色
     */
    @Headers("Content-Type: application/json")
    @POST("characters")
    suspend fun createCharacter(
        @Body request: CreateCharacterRequest,
    ): CreateCharacterResponse

    /**
     * 更新角色信息
     */
    @PUT("characters/{id}")
    suspend fun updateCharacter(
        @Path("id") id: String,
        @Body request: UpdateCharacterRequest,
    ): UpdateCharacterResponse

    /**
     * 删除角色
     */
    @DELETE("characters/{id}")
    suspend fun deleteCharacter(
        @Path("id") id: String,
    )
}

