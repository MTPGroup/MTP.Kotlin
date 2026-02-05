package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatConfigRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper.ChatConfigMapper
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatConfigTable

class ExposedChatConfigRepository : ChatConfigRepository {
    override suspend fun findById(id: tech.hanasaki.azusa.modules.chat.domain.model.ChatConfigId): ChatConfig? =
        dbQuery {
            ChatConfigTable.selectAll()
                .where { ChatConfigTable.id eq id.value }
                .map(ChatConfigMapper::toDomain)
                .singleOrNull()
        }

    override suspend fun findByChatId(chatId: ChatId): ChatConfig? = dbQuery {
        ChatConfigTable.selectAll()
            .where { ChatConfigTable.chatId eq chatId.value }
            .map(ChatConfigMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun save(config: ChatConfig): Unit = dbQuery {
        val updatedRows = ChatConfigTable.update({ ChatConfigTable.id eq config.id.value }) {
            ChatConfigMapper.toEntity(config, it)
            it[ChatConfigTable.updatedAt] = config.updatedAt
        }
        if (updatedRows == 0) {
            ChatConfigTable.insert {
                it[ChatConfigTable.id] = config.id.value
                ChatConfigMapper.toEntity(config, it)
                it[ChatConfigTable.createdAt] = config.createdAt
                it[ChatConfigTable.updatedAt] = config.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: tech.hanasaki.azusa.modules.chat.domain.model.ChatConfigId): Unit = dbQuery {
        ChatConfigTable.deleteWhere { ChatConfigTable.id eq id.value }
    }

    override suspend fun deleteByChatId(chatId: ChatId): Unit = dbQuery {
        ChatConfigTable.deleteWhere { ChatConfigTable.chatId eq chatId.value }
    }
}
