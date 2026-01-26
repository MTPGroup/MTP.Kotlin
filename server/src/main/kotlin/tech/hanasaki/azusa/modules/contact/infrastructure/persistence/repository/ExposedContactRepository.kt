package tech.hanasaki.azusa.modules.contact.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.contact.domain.model.Contact
import tech.hanasaki.azusa.modules.contact.domain.repository.ContactRepository
import tech.hanasaki.azusa.modules.contact.infrastructure.persistence.mapper.ContactMapper
import tech.hanasaki.azusa.modules.contact.infrastructure.persistence.table.ContactTable
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.PageResult
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedContactRepository : ContactRepository {
    override suspend fun find(
        userId: UserId,
        characterId: CharacterId,
    ): Contact? = dbQuery {
        ContactTable.selectAll()
            .where {
                (ContactTable.userId eq userId.value) and
                        (ContactTable.characterId eq characterId.value)
            }
            .map(ContactMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun findByUserIdPaged(
        userId: UserId,
        page: Int,
        limit: Int,
    ): PageResult<Contact> = dbQuery {
        val total = ContactTable.selectAll()
            .where { ContactTable.userId eq userId.value }
            .count()
        val items = ContactTable.selectAll()
            .where { ContactTable.userId eq userId.value }
            .orderBy(ContactTable.addedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(ContactMapper::toDomain)

        PageResult(items, total, page, limit)
    }

    override suspend fun save(contact: Contact): Unit = dbQuery {
        val updated =
            ContactTable.update({
                (ContactTable.userId eq contact.userId.value) and
                        (ContactTable.characterId eq contact.characterId.value)
            }) {
                ContactMapper.toEntity(contact, it)
            }

        if (updated == 0) {
            ContactTable.insert {
                ContactMapper.toEntity(contact, it)
                it[addedAt] = contact.addedAt
            }
        }
    }

    override suspend fun delete(
        userId: UserId,
        characterId: CharacterId,
    ): Unit = dbQuery {
        ContactTable.deleteWhere {
            (ContactTable.userId eq userId.value) and
                    (ContactTable.characterId eq characterId.value)
        }
    }
}