package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable
import java.util.*

class ProfileDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileDao>(ProfileTable)

    var username by ProfileTable.username
    var avatar by ProfileTable.avatar
    var createdAt by ProfileTable.createdAt
    var updatedAt by ProfileTable.updatedAt
}