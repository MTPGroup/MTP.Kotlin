package tech.hanasaki.azusa.modules.chat.application.service

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatUseCasePort
import tech.hanasaki.azusa.modules.chat.domain.model.*
import tech.hanasaki.azusa.modules.chat.domain.port.ChatMemberRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.modules.plugin.domain.port.PluginRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class ChatService(
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatMemberRepository: ChatMemberRepositoryPort,
    private val pluginRepository: PluginRepositoryPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : ChatUseCasePort {

    override suspend fun createChat(userId: UserId, characterId: CharacterId, name: String?, temporary: Boolean): Chat =
        tx.execute {
            val chat =
                Chat.createPrivateChat(ownerId = userId, characterId = characterId, name = name, temporary = temporary)
            chatRepository.save(chat)
            chat.members.forEach { chatMemberRepository.save(it) }
            chat.publishAndClear(domainEventBus)
            chat
        }

    override suspend fun getChat(userId: UserId, chatId: ChatId): Chat = tx.readOnly {
        requireOwner(userId, chatId)
    }

    override suspend fun listChats(userId: UserId, page: Int, limit: Int): PageResult<Chat> = tx.readOnly {
        chatRepository.findByOwnerIdPaged(userId, page, limit)
    }

    override suspend fun deleteChat(userId: UserId, chatId: ChatId) = tx.execute {
        requireOwner(userId, chatId)
        messageRepository.deleteByChatId(chatId)
        chatMemberRepository.deleteByChatId(chatId)
        chatRepository.deleteById(chatId)
    }

    override suspend fun updateChatName(userId: UserId, chatId: ChatId, name: String?) = tx.execute {
        val chat = requireOwner(userId, chatId)
        chat.updateName(name)
        chatRepository.save(chat)
    }

    override suspend fun getMessages(userId: UserId, chatId: ChatId, page: Int, limit: Int): PageResult<Message> =
        tx.readOnly {
            requireOwner(userId, chatId)
            messageRepository.findByChatIdPaged(chatId, page, limit)
        }

    override suspend fun deleteMessages(
        userId: UserId,
        chatId: ChatId,
    ) = tx.execute {
        requireOwner(userId, chatId)
        messageRepository.deleteByChatId(chatId)
    }

    override suspend fun deleteMessage(
        userId: UserId,
        chatId: ChatId,
        messageId: MessageId,
    ) = tx.execute {
        val chat = requireOwner(userId, chatId)
        messageRepository.deleteById(messageId)

        val lastMsg = messageRepository.findLastByChatId(chatId)
        chat.updateLastMessage(lastMsg?.getPlainText()?.take(100) ?: "")
        chatRepository.save(chat)
    }

    // Config

    override suspend fun getConfig(userId: UserId, chatId: ChatId): ChatConfig? = tx.readOnly {
        val chat = requireOwner(userId, chatId)
        chat.config
    }

    override suspend fun updateConfig(
        userId: UserId,
        chatId: ChatId,
        temperature: Double?,
        maxTokens: Int?,
        topP: Double?,
        systemPrompt: String?,
    ): ChatConfig = tx.execute {
        val chat = requireOwner(userId, chatId)
        val config = chat.updateConfig(temperature, maxTokens, topP, systemPrompt)
        chatRepository.save(chat)
        config
    }

    //Plugin subscriptions

    override suspend fun getPluginSubscriptions(userId: UserId, chatId: ChatId): List<ChatPluginSubscription> =
        tx.readOnly {
            val chat = requireOwner(userId, chatId)
            chat.pluginSubscriptions
        }

    override suspend fun togglePlugin(
        userId: UserId,
        chatId: ChatId,
        pluginId: PluginId,
        enabled: Boolean,
    ): ChatPluginSubscription = tx.execute {
        val chat = requireOwner(userId, chatId)
        pluginRepository.findById(pluginId) ?: throw NotFoundException("插件不存在")
        val subscription = chat.togglePlugin(pluginId, enabled)
        chatRepository.save(chat)
        subscription
    }

    override suspend fun updatePluginConfig(
        userId: UserId,
        chatId: ChatId,
        pluginId: PluginId,
        config: JsonObject,
    ): ChatPluginSubscription = tx.execute {
        val chat = requireOwner(userId, chatId)
        val subscription = chat.findPluginSubscription(pluginId)
            ?: throw NotFoundException("插件订阅不存在")
        subscription.updateConfig(config)
        chatRepository.save(chat)
        subscription
    }

    private suspend fun requireOwner(userId: UserId, chatId: ChatId): Chat {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        if (chat.ownerId != userId) throw AuthorizationException("无权操作此会话")
        return chat
    }
}
