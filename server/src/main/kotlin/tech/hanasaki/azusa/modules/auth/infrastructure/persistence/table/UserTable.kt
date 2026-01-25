package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus

object UserTable : Table("users") {
    val id = uuid("id")
    val email = text("email")
    val passwordHash = text("password_hash")
    val status = enumerationByName<UserStatus>("status", 20)
    val emailVerified = bool("email_verified")
    val bannedUntil = timestamp("banned_until").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
