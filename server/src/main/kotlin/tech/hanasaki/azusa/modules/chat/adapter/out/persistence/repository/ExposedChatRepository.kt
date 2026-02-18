package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper.ChatConfigMapper
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper.ChatMapper
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper.ChatPluginSubscriptionMapper
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.ChatConfigTable
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.ChatPluginSubscriptionTable
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.ChatTable
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.port.ChatMemberRepositoryPort
import tech.hanasaki.azusa.modules.chat.domain.port.ChatRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class ExposedChatRepository(
    private val chatMemberRepository: ChatMemberRepositoryPort,
) : ChatRepositoryPort {
    override suspend fun findById(id: ChatId): Chat? {
        val chatRow = ChatTable.selectAll()
            .where { ChatTable.id eq id.value }
            .singleOrNull() ?: return null

        val members = chatMemberRepository.findByChatId(id)

        val config = ChatConfigTable.selectAll()
            .where { ChatConfigTable.chatId eq id.value }
            .singleOrNull()
            ?.let { ChatConfigMapper.toDomain(it) }

        val pluginSubscriptions = ChatPluginSubscriptionTable.selectAll()
            .where { ChatPluginSubscriptionTable.chatId eq id.value }
            .map { ChatPluginSubscriptionMapper.toDomain(it) }

        return ChatMapper.toDomain(chatRow, members, config, pluginSubscriptions)
    }

    override suspend fun save(chat: Chat) {
        val updatedRows = ChatTable.update({ ChatTable.id eq chat.id.value }) {
            ChatMapper.toEntity(chat, it)
        }
        if (updatedRows == 0) {
            ChatTable.insert {
                it[ChatTable.id] = chat.id.value
                ChatMapper.toEntity(chat, it)
                it[ChatTable.createdAt] = chat.createdAt
                it[ChatTable.updatedAt] = chat.updatedAt
            }
        }

        chat.members.forEach { member ->
            chatMemberRepository.save(member)
        }

        // Save config
        val config = chat.config
        if (config != null) {
            val configUpdated = ChatConfigTable.update({ ChatConfigTable.chatId eq chat.id.value }) {
                ChatConfigMapper.toEntity(config, it)
                it[ChatConfigTable.chatId] = chat.id.value
                it[ChatConfigTable.updatedAt] = config.updatedAt
            }
            if (configUpdated == 0) {
                ChatConfigTable.insert {
                    it[ChatConfigTable.id] = kotlin.uuid.Uuid.random()
                    it[ChatConfigTable.chatId] = chat.id.value
                    ChatConfigMapper.toEntity(config, it)
                    it[ChatConfigTable.createdAt] = config.createdAt
                    it[ChatConfigTable.updatedAt] = config.updatedAt
                }
            }
        }

        // Save plugin subscriptions
        for (sub in chat.pluginSubscriptions) {
            val subUpdated = ChatPluginSubscriptionTable.update({ ChatPluginSubscriptionTable.id eq sub.id.value }) {
                ChatPluginSubscriptionMapper.toEntity(sub, it)
                it[ChatPluginSubscriptionTable.chatId] = chat.id.value
            }
            if (subUpdated == 0) {
                ChatPluginSubscriptionTable.insert {
                    it[ChatPluginSubscriptionTable.id] = sub.id.value
                    it[ChatPluginSubscriptionTable.chatId] = chat.id.value
                    ChatPluginSubscriptionMapper.toEntity(sub, it)
                    it[ChatPluginSubscriptionTable.createdAt] = sub.createdAt
                }
            }
        }
    }

    override suspend fun deleteById(id: ChatId) {
        ChatPluginSubscriptionTable.deleteWhere { ChatPluginSubscriptionTable.chatId eq id.value }
        ChatConfigTable.deleteWhere { ChatConfigTable.chatId eq id.value }
        ChatTable.deleteWhere { ChatTable.id eq id.value }
    }

    override suspend fun findByOwnerIdPaged(ownerId: UserId, page: Int, limit: Int): PageResult<Chat> {
        val total = ChatTable.selectAll()
            .where { ChatTable.ownerId eq ownerId.value }
            .count()

        val chatRows = ChatTable.selectAll()
            .where { ChatTable.ownerId eq ownerId.value }
            .orderBy(ChatTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .toList()

        val chats = chatRows.map { chatRow ->
            val chatId = ChatId(chatRow[ChatTable.id])
            val members = chatMemberRepository.findByChatId(chatId)

            val config = ChatConfigTable.selectAll()
                .where { ChatConfigTable.chatId eq chatId.value }
                .singleOrNull()
                ?.let { ChatConfigMapper.toDomain(it) }

            val pluginSubscriptions = ChatPluginSubscriptionTable.selectAll()
                .where { ChatPluginSubscriptionTable.chatId eq chatId.value }
                .map { ChatPluginSubscriptionMapper.toDomain(it) }

            ChatMapper.toDomain(chatRow, members, config, pluginSubscriptions)
        }

        return PageResult(chats, total, page, limit)
    }
}
