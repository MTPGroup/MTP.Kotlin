package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.MessageEntity

@Dao
interface MessageDao {
    /**
     * 根据聊天ID获取消息列表
     */
    @Query("SELECT * FROM MessageEntity WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>

    /**
     * 根据聊天ID获取限制数量的消息
     */
    @Query("SELECT * FROM MessageEntity WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT :limit")
    fun getMessagesByChatIdWithLimit(chatId: String, limit: Int): Flow<List<MessageEntity>>

    /**
     * 插入或更新消息
     */
    @Upsert
    suspend fun upsert(message: MessageEntity)

    /**
     * 批量插入或更新消息
     */
    @Upsert
    suspend fun upsertAll(messages: List<MessageEntity>)

    /**
     * 删除指定聊天的所有消息
     */
    @Query("DELETE FROM MessageEntity WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    /**
     * 删除所有消息
     */
    @Query("DELETE FROM MessageEntity")
    suspend fun deleteAll()
}

