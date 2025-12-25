package tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.ChatEntity

class ChatDao {
    private val chats = MutableStateFlow<List<ChatEntity>>(emptyList())

    fun getAllChats(): Flow<List<ChatEntity>> = chats

    fun getChatById(chatId: String): Flow<ChatEntity?> =
        chats.map { list -> list.firstOrNull { it.id == chatId } }

    suspend fun upsert(chat: ChatEntity) {
        chats.value = chats.value.filterNot { it.id == chat.id } + chat
    }

    suspend fun upsertAll(chatsToUpsert: List<ChatEntity>) {
        val merged = chats.value.associateBy { it.id }.toMutableMap()
        chatsToUpsert.forEach { merged[it.id] = it }
        chats.value = merged.values.sortedByDescending { it.updatedAt }
    }

    suspend fun deleteChat(chatId: String) {
        chats.value = chats.value.filterNot { it.id == chatId }
    }

    suspend fun deleteAll() {
        chats.value = emptyList()
    }
}
