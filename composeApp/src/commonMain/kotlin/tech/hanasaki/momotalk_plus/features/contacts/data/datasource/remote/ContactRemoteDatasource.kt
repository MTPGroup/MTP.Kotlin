package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote

import io.ktor.client.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.BaseRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.model.ContactListResponse

class ContactRemoteDatasource(client: HttpClient) : BaseRemoteDatasource(client) {
    private val endpoint = "$baseUrl/contacts"

    /**
     * 添加联系人
     *
     * @param characterId 角色ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun addContact(characterId: String): IResult<Unit, AppError> =
        post<Any>(
            "$endpoint/$characterId",
            requestBody = null,
        ).map { }

    /**
     * 删除联系人
     *
     * @param characterId 角色ID
     *
     * @return IResult<[Unit], [AppError]>
     */
    suspend fun deleteContact(characterId: String): IResult<Unit, AppError> =
        delete<Any>(
            "$endpoint/$characterId",
        ).map { }

    /**
     * 获取联系人列表
     *
     * @return IResult<[ContactListResponse], [AppError]>
     */
    suspend fun getContacts(): IResult<ContactListResponse, AppError> =
        get(
            endpoint,
        )
}