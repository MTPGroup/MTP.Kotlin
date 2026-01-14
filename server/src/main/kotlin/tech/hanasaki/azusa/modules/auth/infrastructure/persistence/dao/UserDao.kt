package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserTable
import java.util.*

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(UserTable)

    var email by UserTable.email
    var passwordHash by UserTable.passwordHash
    var rawUserMetaData by UserTable.rawUserMetaData
    var updatedAt by UserTable.updatedAt
}