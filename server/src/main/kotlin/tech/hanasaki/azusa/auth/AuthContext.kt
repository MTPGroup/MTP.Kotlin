package tech.hanasaki.azusa.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.*
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
    val principal = principal<UserPrincipal>()
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Unauthorized")

    val profileId = dbQuery {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.uid eq EntityID(principal.userId, UsersTable) }
            .limit(1)
            .map { it[ProfilesTable.id].value }
            .singleOrNull()
    } ?: throw ApiException(HttpStatusCode.NotFound, "PROFILE_NOT_FOUND", "Profile not found")

    return AuthContext(principal.userId, profileId).also {
        attributes.put(AuthContextKey, it)
    }
}
