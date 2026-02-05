package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatTable
import tech.hanasaki.azusa.common.domain.model.UserId

object ChatMapper {
    fun toDomain(row: ResultRow, members: List<tech.hanasaki.azusa.modules.chat.domain.model.ChatMember>): Chat =
        Chat.reconstitute(
            id = tech.hanasaki.azusa.modules.chat.domain.model.ChatId(row[ChatTable.id]),
            ownerId = UserId(row[ChatTable.ownerId]),
            name = row[ChatTable.name],
            lastMessage = row[ChatTable.lastMessage],
            createdAt = row[ChatTable.createdAt],
            updatedAt = row[ChatTable.updatedAt],
            members = members,
        )

    fun toEntity(domain: Chat, target: UpdateBuilder<*>) {
        target[ChatTable.ownerId] = domain.ownerId.value
        target[ChatTable.name] = domain.name
        target[ChatTable.lastMessage] = domain.lastMessage
        target[ChatTable.updatedAt] = domain.updatedAt
    }
}
