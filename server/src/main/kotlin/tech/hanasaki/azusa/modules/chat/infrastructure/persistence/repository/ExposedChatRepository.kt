package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatMemberRepository
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper.ChatMapper
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatTable

class ExposedChatRepository(
    private val chatMemberRepository: ChatMemberRepository,
) : ChatRepository {
    override suspend fun findById(id: ChatId): Chat? {
        val chatRow = ChatTable.selectAll()
            .where { ChatTable.id eq id.value }
            .singleOrNull() ?: return null

        val members = chatMemberRepository.findByChatId(id)

        return ChatMapper.toDomain(chatRow, members)
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
    }

    override suspend fun deleteById(id: ChatId) {
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
            ChatMapper.toDomain(chatRow, members)
        }

        return PageResult(chats, total, page, limit)
    }
}
