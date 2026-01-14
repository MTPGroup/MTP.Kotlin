package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", UserTable)

    val tokenHash = varchar("token_hash", 64).index()

    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    val isRevoked = bool("is_revoked").default(false)
}