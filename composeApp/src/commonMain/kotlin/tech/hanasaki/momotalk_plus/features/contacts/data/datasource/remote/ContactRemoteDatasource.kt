package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.utils.Constants
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.model.ContactListResponse
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.ContactError

class ContactRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "${Constants.BASE_URL}/contact"

    /**
     * 添加联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, UserError>
     */
    suspend fun addContact(userId: String): IResult<Unit, ContactError> {
        return try {
            client.post("$endpoint/add/$userId")
            IResult.Success(Unit)
        } catch (e: Exception) {
            IResult.Error(ContactError.ApiError(-1, e.message ?: "添加联系人失败"))
        }
    }

    /**
     * 删除联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, UserError>
     */
    suspend fun deleteContact(userId: String): IResult<Unit, ContactError> {
        return try {
            client.delete("$endpoint/delete/$userId")
            IResult.Success(Unit)
        } catch (e: Exception) {
            IResult.Error(ContactError.ApiError(-1, e.message ?: "删除联系人失败"))
        }
    }

    /**
     * 获取联系人列表
     *
     * @return Result<List<Contact>, UserError>
     */
    suspend fun getContacts(): IResult<List<Contact>, ContactError> {
        return try {
            val contactsResponse: ContactListResponse = client.get("$endpoint/list").body()
            IResult.Success(contactsResponse.data.contacts)
        } catch (e: Exception) {
            IResult.Error(ContactError.ApiError(-1, e.message ?: "获取联系人列表失败"))
        }
    }
}