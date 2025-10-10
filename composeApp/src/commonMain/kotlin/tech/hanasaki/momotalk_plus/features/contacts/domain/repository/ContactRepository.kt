package tech.hanasaki.momotalk_plus.features.contacts.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact

interface ContactRepository {
    /**
     * 添加联系人
     *
     * @param characterId 角色ID
     *
     */
    suspend fun addContact(characterId: String)

    /**
     * 删除联系人
     *
     * @param characterId 角色ID
     *
     */
    suspend fun deleteContact(characterId: String)

    /**
     * 获取联系人列表
     *
     */
    fun getContacts(): Flow<List<Contact>>
}