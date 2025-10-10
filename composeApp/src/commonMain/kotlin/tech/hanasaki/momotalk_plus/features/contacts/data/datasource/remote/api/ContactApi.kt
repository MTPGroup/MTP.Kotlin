package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.dto.ContactListResponse

interface ContactApi {
    /**
     * 添加联系人
     *
     * @param characterId 角色ID
     *
     */
    @POST("contacts/{characterId}")
    suspend fun addContact(@Path("characterId") characterId: String)

    /**
     * 删除联系人
     *
     * @param characterId 角色ID
     *
     */
    @DELETE("contacts/{characterId}")
    suspend fun deleteContact(@Path("characterId") characterId: String)

    /**
     * 获取联系人列表
     */
    @GET("contacts")
    suspend fun getContacts(): ContactListResponse
}