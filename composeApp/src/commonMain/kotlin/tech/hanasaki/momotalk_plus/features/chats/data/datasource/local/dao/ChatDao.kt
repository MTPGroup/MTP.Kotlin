package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.ChatEntity

@Dao
interface ChatDao {
    /**
     * 获取所有聊天列表
     */
    @Query("SELECT * FROM ChatEntity ORDER BY updatedAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    /**
     * 根据ID获取聊天
     */
    @Query("SELECT * FROM ChatEntity WHERE id = :chatId")
    fun getChatById(chatId: String): Flow<ChatEntity?>

    /**
     * 插入或更新聊天
     */
    @Upsert
    suspend fun upsert(chat: ChatEntity)

    /**
     * 批量插入或更新聊天
     */
    @Upsert
    suspend fun upsertAll(chats: List<ChatEntity>)

    /**
     * 删除聊天
     */
    @Query("DELETE FROM ChatEntity WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)

    /**
     * 删除所有聊天
     */
    @Query("DELETE FROM ChatEntity")
    suspend fun deleteAll()
}

