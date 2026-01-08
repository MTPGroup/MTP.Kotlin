package tech.hanasaki.azusa.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.ProfilesTable
import tech.hanasaki.azusa.db.SettingsTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import java.util.*

data class AuthUser(
    val userId: UUID,
    val email: String,
    val profileId: UUID,
    val username: String,
    val avatar: String?,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

object AuthService {
    private const val bcryptCost = 12

    suspend fun register(email: String, password: String, name: String): AuthUser {
        val normalizedEmail = email.trim().lowercase()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val hashedPassword = BCrypt.withDefaults().hashToString(bcryptCost, password.toCharArray())

        return dbQuery {
            val existing = UsersTable
                .selectAll()
                .where { UsersTable.email eq normalizedEmail }
                .limit(1)
                .map { it[UsersTable.id].value }
                .singleOrNull()
            if (existing != null) {
                throw ApiException(HttpStatusCode.Conflict, "EMAIL_EXISTS", "Email already registered")
            }

            val userId = UsersTable.insertAndGetId { row ->
                row[UsersTable.email] = normalizedEmail
                row[UsersTable.passwordHash] = hashedPassword
                row[UsersTable.rawUserMetaData] = JsonObject(
                    mapOf(
                        "name" to JsonPrimitive(name),
                        "emailVerified" to JsonPrimitive(false),
                    ),
                )
                row[UsersTable.createdAt] = now
                row[UsersTable.updatedAt] = now
            }.value

            val profileId = ProfilesTable.insertAndGetId { row ->
                row[ProfilesTable.uid] = EntityID(userId, UsersTable)
                row[ProfilesTable.username] = name
                row[ProfilesTable.avatar] = null
                row[ProfilesTable.createdAt] = now
                row[ProfilesTable.updatedAt] = now
            }.value

            SettingsTable.insert { row ->
                row[SettingsTable.ownerId] = EntityID(profileId, ProfilesTable)
                row[SettingsTable.theme] = "system"
                row[SettingsTable.chatModels] = JsonArray(emptyList())
                row[SettingsTable.createdAt] = now
                row[SettingsTable.updatedAt] = now
            }

            AuthUser(
                userId = userId,
                email = normalizedEmail,
                profileId = profileId,
                username = name,
                avatar = null,
                emailVerified = false,
                createdAt = now.toString(),
                updatedAt = now.toString(),
            )
        }
    }

    suspend fun login(email: String, password: String): AuthUser {
        val normalizedEmail = email.trim().lowercase()
        return dbQuery {
            val userRow = UsersTable
                .selectAll()
                .where { UsersTable.email eq normalizedEmail }
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Invalid credentials")

            val userId = userRow[UsersTable.id].value
            val passwordHash = userRow[UsersTable.passwordHash]
            val verified = BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
            if (!verified) {
                throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Invalid credentials")
            }

            val profileRow = ProfilesTable
                .selectAll()
                .where { ProfilesTable.uid eq EntityID(userId, UsersTable) }
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.NotFound, "PROFILE_NOT_FOUND", "Profile not found")
            val emailVerified = extractEmailVerified(userRow[UsersTable.rawUserMetaData])

            AuthUser(
                userId = userId,
                email = normalizedEmail,
                profileId = profileRow[ProfilesTable.id].value,
                username = profileRow[ProfilesTable.username],
                avatar = profileRow[ProfilesTable.avatar],
                emailVerified = emailVerified,
                createdAt = profileRow[ProfilesTable.createdAt].toString(),
                updatedAt = profileRow[ProfilesTable.updatedAt].toString(),
            )
        }
    }

    private fun extractEmailVerified(meta: JsonElement): Boolean {
        val obj = meta as? JsonObject ?: return false
        val value = obj["emailVerified"] as? JsonPrimitive ?: return false
        return value.booleanOrNull ?: false
    }
}
