package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.MessageEntity

class MessageDao {
    private val messages = MutableStateFlow<List<MessageEntity>>(emptyList())

    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>> =
        messages.map { list -> list.filter { it.chatId == chatId }.sortedBy { it.createdAt } }

    fun getMessagesByChatIdWithLimit(chatId: String, limit: Int): Flow<List<MessageEntity>> =
        messages.map { list -> list.filter { it.chatId == chatId }.sortedByDescending { it.createdAt }.take(limit).sortedBy { it.createdAt } }

    suspend fun upsert(message: MessageEntity) {
        messages.value = messages.value.filterNot { it.id == message.id } + message
    }

    suspend fun upsertAll(messageList: List<MessageEntity>) {
        val merged = messages.value.associateBy { it.id }.toMutableMap()
        messageList.forEach { merged[it.id] = it }
        messages.value = merged.values.toList()
    }

    suspend fun deleteMessagesByChatId(chatId: String) {
        messages.value = messages.value.filterNot { it.chatId == chatId }
    }

    suspend fun deleteAll() {
        messages.value = emptyList()
    }
}
