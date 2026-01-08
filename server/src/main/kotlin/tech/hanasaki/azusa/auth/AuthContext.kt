package tech.hanasaki.azusa.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.ProfilesTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import java.util.*

data class AuthContext(
    val userId: UUID,
    val profileId: UUID,
)

private val AuthContextKey = AttributeKey<AuthContext>("authContext")

suspend fun ApplicationCall.requireAuthContext(): AuthContext {
    attributes.getOrNull(AuthContextKey)?.let { return it }
    val principal = principal<JWTPrincipal>()
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Unauthorized")

    val subject = principal.subject
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Unauthorized")
    val userId = runCatching { UUID.fromString(subject) }.getOrNull()
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Unauthorized")

    val profileId = dbQuery {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.uid eq EntityID(userId, UsersTable) }
            .limit(1)
            .map { it[ProfilesTable.id].value }
            .singleOrNull()
    } ?: throw ApiException(HttpStatusCode.NotFound, "PROFILE_NOT_FOUND", "Profile not found")

    val emailVerified = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .limit(1)
            .map { extractEmailVerified(it[UsersTable.rawUserMetaData]) }
            .singleOrNull()
    } ?: false
    if (!emailVerified) {
        throw ApiException(HttpStatusCode.Forbidden, "EMAIL_NOT_VERIFIED", "Email not verified")
    }

    return AuthContext(userId, profileId).also {
        attributes.put(AuthContextKey, it)
    }
}

private fun extractEmailVerified(meta: JsonElement): Boolean {
    val obj = meta as? JsonObject ?: return false
    val value = obj["emailVerified"] as? JsonPrimitive ?: return false
    return value.booleanOrNull ?: false
}
