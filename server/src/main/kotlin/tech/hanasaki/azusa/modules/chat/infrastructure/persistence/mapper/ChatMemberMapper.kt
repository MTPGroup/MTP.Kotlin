package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.chat.domain.model.ChatMember
import tech.hanasaki.azusa.modules.chat.domain.model.MemberRole
import tech.hanasaki.azusa.modules.chat.domain.model.MemberType
import tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table.ChatMemberTable
import tech.hanasaki.azusa.common.domain.model.CharacterId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.chat.domain.model.ChatMemberId

object ChatMemberMapper {
    fun toDomain(row: ResultRow): ChatMember = ChatMember(
        id = ChatMemberId(row[ChatMemberTable.id]),
        chatId = ChatId(row[ChatMemberTable.chatId]),
        memberType = MemberType.valueOf(row[ChatMemberTable.memberType]),
        profileId = row[ChatMemberTable.profileId]?.let { UserId(it) },
        characterId = row[ChatMemberTable.characterId]?.let { CharacterId(it) },
        role = MemberRole.valueOf(row[ChatMemberTable.role]),
        joinedAt = row[ChatMemberTable.joinedAt],
        updatedAt = row[ChatMemberTable.updatedAt],
    )

    fun toEntity(domain: ChatMember, target: UpdateBuilder<*>) {
        target[ChatMemberTable.chatId] = domain.chatId.value
        target[ChatMemberTable.memberType] = domain.memberType.name
        target[ChatMemberTable.profileId] = domain.profileId?.value
        target[ChatMemberTable.characterId] = domain.characterId?.value
        target[ChatMemberTable.role] = domain.role.name
        target[ChatMemberTable.updatedAt] = domain.updatedAt
    }
}
