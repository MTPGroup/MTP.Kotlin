package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus

@Serializable
data class UserMetaData(
    val status: UserStatus,
    val emailVerified: Boolean,
    val bannedUntil: Instant?,
)

val format = Json { prettyPrint = true }

object UserTable : UUIDTable("users") {
    val email = text("email")
    val passwordHash = text("password_hash")
    val rawUserMetaData = jsonb<UserMetaData>("raw_user_meta_data", format)
    val createdAt = datetime("created_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))
    val updatedAt = datetime("updated_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))
}
