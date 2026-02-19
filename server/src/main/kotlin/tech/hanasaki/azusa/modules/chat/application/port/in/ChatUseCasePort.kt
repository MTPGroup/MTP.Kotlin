package tech.hanasaki.azusa.modules.chat.application.port.`in`

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface ChatUseCasePort {
    suspend fun createChat(userId: UserId, characterId: CharacterId, name: String?, temporary: Boolean = false): Chat
    suspend fun getChat(userId: UserId, chatId: ChatId): Chat
    suspend fun listChats(userId: UserId, page: Int, limit: Int): PageResult<Chat>
    suspend fun deleteChat(userId: UserId, chatId: ChatId)
    suspend fun updateChatName(userId: UserId, chatId: ChatId, name: String?)
    suspend fun getMessages(userId: UserId, chatId: ChatId, page: Int, limit: Int): PageResult<Message>
    suspend fun deleteMessage(userId: UserId, chatId: ChatId, messageId: MessageId)

    // Config
    suspend fun getConfig(userId: UserId, chatId: ChatId): ChatConfig?
    suspend fun updateConfig(
        userId: UserId,
        chatId: ChatId,
        temperature: Double?,
        maxTokens: Int?,
        topP: Double?,
        systemPrompt: String?,
    ): ChatConfig

    // Plugin subscriptions
    suspend fun getPluginSubscriptions(userId: UserId, chatId: ChatId): List<ChatPluginSubscription>
    suspend fun togglePlugin(userId: UserId, chatId: ChatId, pluginId: PluginId, enabled: Boolean): ChatPluginSubscription
    suspend fun updatePluginConfig(userId: UserId, chatId: ChatId, pluginId: PluginId, config: JsonObject): ChatPluginSubscription
}
