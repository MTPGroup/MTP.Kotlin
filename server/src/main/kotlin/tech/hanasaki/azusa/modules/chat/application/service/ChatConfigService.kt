package tech.hanasaki.azusa.modules.chat.application.service

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatConfigUseCasePort
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.domain.port.ChatConfigRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatPluginSubscriptionRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class ChatConfigService(
    private val chatConfigRepository: ChatConfigRepositoryPort,
    private val chatPluginSubscriptionRepository: ChatPluginSubscriptionRepositoryPort,
    private val chatRepository: ChatRepositoryPort,
    private val pluginRepository: PluginRepositoryPort,
    private val tx: TransactionalPort,
) : ChatConfigUseCasePort {

    override suspend fun getConfig(userId: UserId, chatId: ChatId): ChatConfig? = tx.readOnly {
        requireChatOwner(userId, chatId)
        chatConfigRepository.findByChatId(chatId)
    }

    override suspend fun updateConfig(
        userId: UserId,
        chatId: ChatId,
        temperature: Double?,
        maxTokens: Int?,
        topP: Double?,
        systemPrompt: String?,
    ): ChatConfig = tx.execute {
        requireChatOwner(userId, chatId)
        val config = chatConfigRepository.findByChatId(chatId)
            ?: ChatConfig.create(chatId = chatId)

        config.updateLLMParams(temperature, maxTokens, topP)
        config.updateSystemPrompt(systemPrompt)
        chatConfigRepository.save(config)
        config
    }

    override suspend fun getPluginSubscriptions(userId: UserId, chatId: ChatId): List<ChatPluginSubscription> =
        tx.readOnly {
            requireChatOwner(userId, chatId)
            chatPluginSubscriptionRepository.findByChatId(chatId)
        }

    override suspend fun togglePlugin(
        userId: UserId,
        chatId: ChatId,
        pluginId: PluginId,
        enabled: Boolean,
    ): ChatPluginSubscription = tx.execute {
        requireChatOwner(userId, chatId)
        pluginRepository.findById(pluginId) ?: throw NotFoundException("插件不存在")

        val subscription = chatPluginSubscriptionRepository.findByChatIdAndPluginId(chatId, pluginId)
            ?: ChatPluginSubscription.create(chatId = chatId, pluginId = pluginId, enabled = enabled)

        if (enabled) subscription.enable() else subscription.disable()
        chatPluginSubscriptionRepository.save(subscription)
        subscription
    }

    override suspend fun updatePluginConfig(
        userId: UserId,
        chatId: ChatId,
        pluginId: PluginId,
        config: JsonObject,
    ): ChatPluginSubscription = tx.execute {
        requireChatOwner(userId, chatId)
        val subscription = chatPluginSubscriptionRepository.findByChatIdAndPluginId(chatId, pluginId)
            ?: throw NotFoundException("插件订阅不存在")

        subscription.updateConfig(config)
        chatPluginSubscriptionRepository.save(subscription)
        subscription
    }

    private suspend fun requireChatOwner(userId: UserId, chatId: ChatId) {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        if (chat.ownerId != userId) throw AuthorizationException("无权操作此会话")
    }
}
