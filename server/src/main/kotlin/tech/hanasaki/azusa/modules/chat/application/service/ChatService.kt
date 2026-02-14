package tech.hanasaki.azusa.modules.chat.application.service

import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatUseCasePort
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId
import tech.hanasaki.azusa.modules.chat.domain.port.*
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class ChatService(
    private val chatRepository: ChatRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val chatMemberRepository: ChatMemberRepositoryPort,
    private val chatConfigRepository: ChatConfigRepositoryPort,
    private val chatPluginSubscriptionRepository: ChatPluginSubscriptionRepositoryPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : ChatUseCasePort {

    override suspend fun createChat(userId: UserId, characterId: CharacterId, name: String?): Chat =
        tx.execute {
            val chat = Chat.createPrivateChat(ownerId = userId, characterId = characterId, name = name)
            chatRepository.save(chat)
            chat.members.forEach { chatMemberRepository.save(it) }
            chat.publishAndClear(domainEventBus)
            chat
        }

    override suspend fun getChat(userId: UserId, chatId: ChatId): Chat = tx.readOnly {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        requireOwner(chat, userId)
        chat
    }

    override suspend fun listChats(userId: UserId, page: Int, limit: Int): PageResult<Chat> = tx.readOnly {
        chatRepository.findByOwnerIdPaged(userId, page, limit)
    }

    override suspend fun deleteChat(userId: UserId, chatId: ChatId) = tx.execute {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        requireOwner(chat, userId)
        messageRepository.deleteByChatId(chatId)
        chatConfigRepository.deleteByChatId(chatId)
        chatPluginSubscriptionRepository.deleteByChatId(chatId)
        chatMemberRepository.deleteByChatId(chatId)
        chatRepository.deleteById(chatId)
    }

    override suspend fun updateChatName(userId: UserId, chatId: ChatId, name: String?) = tx.execute {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
        requireOwner(chat, userId)
        chat.updateName(name)
        chatRepository.save(chat)
    }

    override suspend fun getMessages(userId: UserId, chatId: ChatId, page: Int, limit: Int): PageResult<Message> =
        tx.readOnly {
            val chat = chatRepository.findById(chatId) ?: throw NotFoundException("会话不存在")
            requireOwner(chat, userId)
            messageRepository.findByChatIdPaged(chatId, page, limit)
        }

    override suspend fun deleteMessage(
        userId: UserId,
        chatId: ChatId,
        messageId: MessageId,
    ) = tx.execute {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException()
        requireOwner(chat, userId)
        // TODO: 需要同时发布消息删除事件，会话检测是否需要更新最后一条消息
        messageRepository.deleteById(messageId)
    }

    private fun requireOwner(chat: Chat, userId: UserId) {
        if (chat.ownerId != userId) throw AuthorizationException("无权操作此会话")
    }
}
