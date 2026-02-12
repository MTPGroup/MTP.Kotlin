package tech.hanasaki.azusa.modules.chat.application.port.`in`

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface ChatConfigUseCasePort {
    suspend fun getConfig(userId: UserId, chatId: ChatId): ChatConfig?
    suspend fun updateConfig(
        userId: UserId,
        chatId: ChatId,
        temperature: Double?,
        maxTokens: Int?,
        topP: Double?,
        systemPrompt: String?,
    ): ChatConfig

    suspend fun getPluginSubscriptions(userId: UserId, chatId: ChatId): List<ChatPluginSubscription>
    suspend fun togglePlugin(userId: UserId, chatId: ChatId, pluginId: PluginId, enabled: Boolean): ChatPluginSubscription
    suspend fun updatePluginConfig(userId: UserId, chatId: ChatId, pluginId: PluginId, config: JsonObject): ChatPluginSubscription
}
