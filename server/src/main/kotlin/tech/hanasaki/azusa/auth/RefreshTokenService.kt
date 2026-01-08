package tech.hanasaki.azusa.auth

import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.RefreshTokensTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

class RefreshTokenService(private val refreshTokenDays: Int) {
    private val random = SecureRandom()

    suspend fun issue(userId: UUID): String {
        val token = generateToken()
        val nowInstant = Clock.System.now()
        val now = nowInstant.toLocalDateTime(TimeZone.UTC)
        val expiresAt = nowInstant.plus(refreshTokenDays, DateTimeUnit.DAY, TimeZone.UTC)
            .toLocalDateTime(TimeZone.UTC)
        val hash = hashToken(token)

        dbQuery {
            RefreshTokensTable.insertAndGetId { row ->
                row[RefreshTokensTable.userId] = EntityID(userId, UsersTable)
                row[RefreshTokensTable.tokenHash] = hash
                row[RefreshTokensTable.expiresAt] = expiresAt
                row[RefreshTokensTable.createdAt] = now
                row[RefreshTokensTable.revokedAt] = null
                row[RefreshTokensTable.replacedBy] = null
            }
        }

        return token
    }

    suspend fun rotate(token: String): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val hash = hashToken(token)

        return dbQuery {
            val record = RefreshTokensTable
                .selectAll()
                .where {
                    (RefreshTokensTable.tokenHash eq hash) and
                        (RefreshTokensTable.revokedAt.isNull()) and
                        (RefreshTokensTable.expiresAt greaterEq now)
                }
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.Unauthorized, "REFRESH_TOKEN_INVALID", "Invalid refresh token")

            val userId = record[RefreshTokensTable.userId].value
            val newToken = generateToken()
            val newHash = hashToken(newToken)
            val nowInstant = Clock.System.now()
            val newExpiresAt = nowInstant.plus(refreshTokenDays, DateTimeUnit.DAY, TimeZone.UTC)
                .toLocalDateTime(TimeZone.UTC)
            val newId = RefreshTokensTable.insertAndGetId { row ->
                row[RefreshTokensTable.userId] = EntityID(userId, UsersTable)
                row[RefreshTokensTable.tokenHash] = newHash
                row[RefreshTokensTable.expiresAt] = newExpiresAt
                row[RefreshTokensTable.createdAt] = now
                row[RefreshTokensTable.revokedAt] = null
                row[RefreshTokensTable.replacedBy] = null
            }.value

            RefreshTokensTable.update({ RefreshTokensTable.id eq record[RefreshTokensTable.id].value }) {
                it[revokedAt] = now
                it[replacedBy] = EntityID(newId, RefreshTokensTable)
            }

            newToken
        }
    }

    suspend fun revoke(token: String): Unit {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val hash = hashToken(token)
        dbQuery {
            RefreshTokensTable.update({ RefreshTokensTable.tokenHash eq hash }) {
                it[revokedAt] = now
            }
        }
    }

    suspend fun validate(token: String): UUID {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val hash = hashToken(token)
        return dbQuery {
            RefreshTokensTable
                .selectAll()
                .where {
                    (RefreshTokensTable.tokenHash eq hash) and
                        (RefreshTokensTable.revokedAt.isNull()) and
                        (RefreshTokensTable.expiresAt greaterEq now)
                }
                .limit(1)
                .map { it[RefreshTokensTable.userId].value }
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.Unauthorized, "REFRESH_TOKEN_INVALID", "Invalid refresh token")
        }
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
