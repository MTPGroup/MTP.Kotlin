package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.Chat
import tech.hanasaki.azusa.modules.chat.domain.model.ChatConfig
import tech.hanasaki.azusa.modules.chat.domain.model.ChatPluginSubscription
import tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table.ChatTable
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

object ChatMapper {
    fun toDomain(
        row: ResultRow,
        members: List<tech.hanasaki.azusa.modules.chat.domain.model.ChatMember>,
        config: ChatConfig? = null,
        pluginSubscriptions: List<ChatPluginSubscription> = emptyList(),
    ): Chat =
        Chat.reconstitute(
            id = tech.hanasaki.azusa.modules.chat.domain.model.ChatId(row[ChatTable.id]),
            ownerId = UserId(row[ChatTable.ownerId]),
            name = row[ChatTable.name],
            lastMessage = row[ChatTable.lastMessage],
            createdAt = row[ChatTable.createdAt],
            updatedAt = row[ChatTable.updatedAt],
            members = members,
            config = config,
            pluginSubscriptions = pluginSubscriptions,
            temporary = row[ChatTable.temporary],
            expiresAt = row[ChatTable.expiresAt],
        )

    fun toEntity(domain: Chat, target: UpdateBuilder<*>) {
        target[ChatTable.ownerId] = domain.ownerId.value
        target[ChatTable.name] = domain.name
        target[ChatTable.lastMessage] = domain.lastMessage
        target[ChatTable.updatedAt] = domain.updatedAt
        target[ChatTable.temporary] = domain.temporary
        target[ChatTable.expiresAt] = domain.expiresAt
    }
}
