package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)

    val tokenHash = varchar("token_hash", 64).index()

    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    val isRevoked = bool("is_revoked").default(false)

    override val primaryKey = PrimaryKey(id)
}
