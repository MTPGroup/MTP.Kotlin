package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatMember
import tech.hanasaki.azusa.modules.chat.domain.model.ChatMemberId
import tech.hanasaki.azusa.modules.chat.domain.repository.ChatMemberRepository
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper.ChatMemberMapper
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatMemberTable

class ExposedChatMemberRepository : ChatMemberRepository {
    override suspend fun findById(id: ChatMemberId): ChatMember? =
        ChatMemberTable.selectAll()
            .where { ChatMemberTable.id eq id.value }
            .map(ChatMemberMapper::toDomain)
            .singleOrNull()

    override suspend fun findByChatId(chatId: ChatId): List<ChatMember> =
        ChatMemberTable.selectAll()
            .where { ChatMemberTable.chatId eq chatId.value }
            .map(ChatMemberMapper::toDomain)

    override suspend fun save(member: ChatMember) {
        val updatedRows = ChatMemberTable.update({ ChatMemberTable.id eq member.id.value }) {
            ChatMemberMapper.toEntity(member, it)
            it[ChatMemberTable.updatedAt] = member.updatedAt
        }
        if (updatedRows == 0) {
            ChatMemberTable.insert {
                it[ChatMemberTable.id] = member.id.value
                ChatMemberMapper.toEntity(member, it)
                it[ChatMemberTable.joinedAt] = member.joinedAt
                it[ChatMemberTable.updatedAt] = member.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: ChatMemberId) {
        ChatMemberTable.deleteWhere { ChatMemberTable.id eq id.value }
    }

    override suspend fun deleteByChatId(chatId: ChatId) {
        ChatMemberTable.deleteWhere { ChatMemberTable.chatId eq chatId.value }
    }
}
