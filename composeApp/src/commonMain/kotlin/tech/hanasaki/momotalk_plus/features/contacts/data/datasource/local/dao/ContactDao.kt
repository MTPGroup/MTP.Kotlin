package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity.ContactEntity

@Dao
interface ContactDao {
    /**
     * 获取联系人列表
     */
    @Query("SELECT * FROM ContactEntity")
    fun getContacts(): Flow<List<ContactEntity>>

    /**
     * 添加联系人
     */
    @Insert
    suspend fun addContact(contact: ContactEntity)

    /**
     * 插入或更新联系人
     */
    @Upsert
    suspend fun upsert(contact: ContactEntity)

    /**
     * 批量插入或更新联系人
     */
    @Upsert
    suspend fun upsertAll(contacts: List<ContactEntity>)

    /**
     * 删除联系人
     */
    @Query("DELETE FROM ContactEntity WHERE id = :characterId")
    suspend fun deleteContact(characterId: String)

    /**
     * 清空联系人列表
     */
    @Query("DELETE FROM ContactEntity")
    suspend fun deleteAll()
}