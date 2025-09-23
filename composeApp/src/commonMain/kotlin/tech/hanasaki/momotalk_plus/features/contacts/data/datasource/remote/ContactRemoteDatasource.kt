package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.ContactError

class ContactRemoteDatasource(private val client: HttpClient) {
    private val endpoint = "http://localhost:3001/api/contact"

    /**
     * 添加联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, UserError>
     */
    suspend fun addContact(userId: String): Result<Unit, ContactError> {
        return try {
            client.post("$endpoint/add/$userId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ContactError.ApiError(-1, e.message ?: "添加联系人失败"))
        }
    }

    /**
     * 删除联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, UserError>
     */
    suspend fun deleteContact(userId: String): Result<Unit, ContactError> {
        return try {
            client.delete("$endpoint/delete/$userId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ContactError.ApiError(-1, e.message ?: "删除联系人失败"))
        }
    }

    /**
     * 获取联系人列表
     *
     * @return Result<List<Contact>, UserError>
     */
    // TODO: 联系人数据模型
    suspend fun getContacts(): Result<List<Contact>, ContactError> {
        return try {
            val contacts: List<Contact> = client.get("$endpoint/list").body()
            Result.Success(contacts)
        } catch (e: Exception) {
            Result.Error(ContactError.ApiError(-1, e.message ?: "获取联系人列表失败"))
        }
    }
}