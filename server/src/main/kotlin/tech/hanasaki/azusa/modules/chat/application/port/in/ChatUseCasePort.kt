package tech.hanasaki.azusa.modules.chat.application.port.`in`

import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface ChatUseCasePort {
    suspend fun createChat(userId: UserId, characterId: CharacterId, name: String?): Chat
    suspend fun getChat(userId: UserId, chatId: ChatId): Chat
    suspend fun listChats(userId: UserId, page: Int, limit: Int): PageResult<Chat>
    suspend fun deleteChat(userId: UserId, chatId: ChatId)
    suspend fun updateChatName(userId: UserId, chatId: ChatId, name: String?)
    suspend fun getMessages(userId: UserId, chatId: ChatId, page: Int, limit: Int): PageResult<Message>
    suspend fun deleteMessage(userId: UserId, chatId: ChatId, messageId: MessageId)
}
