package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp


object RefreshTokenTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id")

    val tokenHash = varchar("token_hash", 64)

    val isRevoked = bool("is_revoked")
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
