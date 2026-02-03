package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import tech.hanasaki.azusa.modules.chat.domain.model.Message
import tech.hanasaki.azusa.modules.chat.domain.repository.MessageRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper.MessageMapper
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.MessageTable
import tech.hanasaki.azusa.common.kernel.model.PageResult
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.MessageId
import tech.hanasaki.azusa.common.platform.database.dbQuery

class ExposedMessageRepository : MessageRepository {
    override suspend fun save(message: Message): Unit = dbQuery {
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
    ): PageResult<Message> = dbQuery {
        val total = MessageTable.selectAll()
            .where { MessageTable.chatId eq chatId.value }
            .count()

        val messages = MessageTable.selectAll()
            .where { MessageTable.chatId eq chatId.value }
            .orderBy(MessageTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(MessageMapper::toDomain)

        PageResult(messages, total, page, limit)
    }

    override suspend fun findByChatId(chatId: ChatId): List<Message> = dbQuery {
        MessageTable.selectAll()
            .where { MessageTable.chatId eq chatId.value }
            .orderBy(MessageTable.createdAt, SortOrder.ASC)
            .map(MessageMapper::toDomain)
    }

    override suspend fun deleteById(id: MessageId): Unit = dbQuery {
        MessageTable.deleteWhere { MessageTable.id eq id.value }
    }

    override suspend fun deleteByChatId(chatId: ChatId): Unit = dbQuery {
        MessageTable.deleteWhere { MessageTable.chatId eq chatId.value }
    }
}
