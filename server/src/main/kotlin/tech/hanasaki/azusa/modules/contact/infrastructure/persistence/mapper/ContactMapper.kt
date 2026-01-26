package tech.hanasaki.azusa.modules.contact.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.contact.domain.model.Contact
import tech.hanasaki.azusa.modules.contact.infrastructure.persistence.table.ContactTable
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId

object ContactMapper {
    fun toDomain(row: ResultRow): Contact = Contact(
        userId = UserId(row[ContactTable.userId]),
        characterId = CharacterId(row[ContactTable.characterId]),
        nickname = row[ContactTable.nickname],
        addedAt = row[ContactTable.addedAt]
    )

    fun toEntity(domain: Contact, target: UpdateBuilder<*>) {
        target[ContactTable.userId] = domain.userId.value
        target[ContactTable.characterId] = domain.characterId.value
        target[ContactTable.nickname] = domain.nickname
    }
}