package tech.hanasaki.momotalk_plus.features.contacts.domain.repository

import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.ContactError

interface ContactRepository {
    /**
     * 添加联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, ContactError>
     */
    suspend fun addContact(userId: String): Result<Unit, ContactError>

    /**
     * 删除联系人
     *
     * @param userId 用户ID
     * @return Result<Unit, ContactError>
     */
    suspend fun deleteContact(userId: String): Result<Unit, ContactError>

    /**
     * 获取联系人列表
     *
     * @return Result<List<Contact>, ContactError>
     */
    suspend fun getContacts(): Result<List<Contact>, ContactError>
}