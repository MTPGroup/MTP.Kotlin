package tech.hanasaki.azusa.modules.contact.domain.repository

import tech.hanasaki.azusa.modules.contact.domain.model.Contact
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.PageResult
import tech.hanasaki.azusa.shared.domain.model.UserId

interface ContactRepository {
    suspend fun find(
        userId: UserId, characterId: CharacterId,
    ): Contact?

    suspend fun findByUserIdPaged(userId: UserId, page: Int, limit: Int): PageResult<Contact>

    suspend fun save(contact: Contact)
    suspend fun delete(userId: UserId, characterId: CharacterId)
}