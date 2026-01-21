package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class UserMetaData(
    val status: UserStatus,
    val emailVerified: Boolean,
    val bannedUntil: Instant?,
)

val format = Json { prettyPrint = true }

object UserTable : Table("users") {
    val id = uuid("id")
    val email = text("email")
    val passwordHash = text("password_hash")
    val rawUserMetaData = jsonb<UserMetaData>("raw_user_meta_data", format)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
