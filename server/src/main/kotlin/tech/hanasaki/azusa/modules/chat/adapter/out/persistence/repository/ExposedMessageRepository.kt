package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId
import tech.hanasaki.azusa.modules.chat.domain.port.MessageRepositoryPort
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper.MessageMapper
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.MessageTable

class ExposedMessageRepository : MessageRepositoryPort {
    override suspend fun save(message: Message) {
        MessageTable.insert {
            it[MessageTable.id] = message.id.value
            MessageMapper.toEntity(message, it)
            it[MessageTable.createdAt] = message.createdAt
            it[MessageTable.updatedAt] = message.createdAt
        }
    }

    override suspend fun findByChatIdPaged(
        chatId: ChatId,
        page: Int,
        limit: Int,
    ): PageResult<Message> {
        val total = MessageTable.selectAll()
            .where { MessageTable.chatId eq chatId.value }
            .count()

        val messages = MessageTable.selectAll()
            .where { MessageTable.chatId eq chatId.value }
            .orderBy(MessageTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(MessageMapper::toDomain)

        return PageResult(messages, total, page, limit)
    }

    override suspend fun findByChatId(chatId: ChatId): List<Message> = MessageTable.selectAll()
        .where { MessageTable.chatId eq chatId.value }
        .orderBy(MessageTable.createdAt, SortOrder.ASC)
        .map(MessageMapper::toDomain)

    override suspend fun findById(id: MessageId): Message? = MessageTable.selectAll()
        .where { MessageTable.id eq id.value }
        .singleOrNull()
        ?.let(MessageMapper::toDomain)

    override suspend fun findLastByChatId(chatId: ChatId): Message? = MessageTable.selectAll()
        .where { MessageTable.chatId eq chatId.value }
        .orderBy(MessageTable.createdAt, SortOrder.DESC)
        .limit(1)
        .singleOrNull()
        ?.let(MessageMapper::toDomain)

    override suspend fun deleteById(id: MessageId) {
        MessageTable.deleteWhere { MessageTable.id eq id.value }
    }

    override suspend fun deleteByChatId(chatId: ChatId) {
        MessageTable.deleteWhere { MessageTable.chatId eq chatId.value }
    }
}
