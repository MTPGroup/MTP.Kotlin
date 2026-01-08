package tech.hanasaki.azusa.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.EmailOtpsTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class OtpType(val value: String) {
    SIGN_IN("sign-in"),
    RESET_PASSWORD("reset-password"),
    VERIFY_EMAIL("email-verification"),
    ;

    companion object {
        fun fromValue(value: String): OtpType? =
            entries.firstOrNull { it.value == value }
    }
}

data class OtpConfig(
    val length: Int,
    val expiresMinutes: Int,
    val minIntervalSeconds: Int,
    val maxPerHour: Int,
)

class EmailOtpService(private val config: OtpConfig) {
    private val bcryptCost = 10

    suspend fun createOtp(email: String, type: OtpType): String {
        val normalizedEmail = email.trim().lowercase()
        val nowInstant = Clock.System.now()
        val now = nowInstant.toLocalDateTime(TimeZone.UTC)
        val minIntervalThreshold = nowInstant.minus(config.minIntervalSeconds.seconds)
            .toLocalDateTime(TimeZone.UTC)
        val hourThreshold = nowInstant.minus(1.hours).toLocalDateTime(TimeZone.UTC)

        dbQuery {
            val lastSentAt = EmailOtpsTable
                .selectAll()
                .where { (EmailOtpsTable.email eq normalizedEmail) and (EmailOtpsTable.type eq type.value) }
                .orderBy(EmailOtpsTable.createdAt, org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(1)
                .map { it[EmailOtpsTable.createdAt] }
                .singleOrNull()
            if (lastSentAt != null && lastSentAt >= minIntervalThreshold) {
                throw ApiException(HttpStatusCode.TooManyRequests, "OTP_TOO_FREQUENT", "OTP requested too frequently")
            }

            val countLastHour = EmailOtpsTable
                .selectAll()
                .where {
                    (EmailOtpsTable.email eq normalizedEmail) and
                        (EmailOtpsTable.type eq type.value) and
                        (EmailOtpsTable.createdAt greaterEq hourThreshold)
                }
                .count()
            if (countLastHour >= config.maxPerHour.toLong()) {
                throw ApiException(HttpStatusCode.TooManyRequests, "OTP_RATE_LIMIT", "OTP rate limit exceeded")
            }
        }

        val code = generateCode(config.length)
        val codeHash = BCrypt.withDefaults().hashToString(bcryptCost, code.toCharArray())
        val expiresAt = nowInstant.plus(config.expiresMinutes.minutes)
            .toLocalDateTime(TimeZone.UTC)

        dbQuery {
            EmailOtpsTable.insert { row ->
                row[EmailOtpsTable.email] = normalizedEmail
                row[EmailOtpsTable.type] = type.value
                row[EmailOtpsTable.codeHash] = codeHash
                row[EmailOtpsTable.expiresAt] = expiresAt
                row[EmailOtpsTable.createdAt] = now
                row[EmailOtpsTable.usedAt] = null
            }
        }

        return code
    }

    suspend fun verifyOtp(email: String, type: OtpType, otp: String): Unit {
        val normalizedEmail = email.trim().lowercase()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        dbQuery {
            val record = EmailOtpsTable
                .selectAll()
                .where {
                    (EmailOtpsTable.email eq normalizedEmail) and
                        (EmailOtpsTable.type eq type.value) and
                        (EmailOtpsTable.usedAt.isNull()) and
                        (EmailOtpsTable.expiresAt greaterEq now)
                }
                .orderBy(EmailOtpsTable.createdAt, org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.BadRequest, "OTP_INVALID", "Invalid or expired OTP")

            val hash = record[EmailOtpsTable.codeHash]
            val verified = BCrypt.verifyer().verify(otp.toCharArray(), hash).verified
            if (!verified) {
                throw ApiException(HttpStatusCode.BadRequest, "OTP_INVALID", "Invalid or expired OTP")
            }

            EmailOtpsTable.update({ EmailOtpsTable.id eq record[EmailOtpsTable.id].value }) {
                it[usedAt] = now
            }
        }
    }

    suspend fun markEmailVerified(email: String): Unit {
        val normalizedEmail = email.trim().lowercase()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        dbQuery {
            val user = UsersTable
                .selectAll()
                .where { UsersTable.email eq normalizedEmail }
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "User not found")

            val userId = user[UsersTable.id].value
            val currentMeta = user[UsersTable.rawUserMetaData]
            val base = (currentMeta as? JsonObject)?.toMutableMap() ?: mutableMapOf()
            base["emailVerified"] = JsonPrimitive(true)
            UsersTable.update({ UsersTable.id eq userId }) {
                it[rawUserMetaData] = JsonObject(base)
                it[updatedAt] = now
            }
        }
    }

    suspend fun resetPassword(email: String, newPasswordHash: String): Unit {
        val normalizedEmail = email.trim().lowercase()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        dbQuery {
            val user = UsersTable
                .selectAll()
                .where { UsersTable.email eq normalizedEmail }
                .limit(1)
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "User not found")

            val userId = user[UsersTable.id].value
            UsersTable.update({ UsersTable.id eq userId }) {
                it[passwordHash] = newPasswordHash
                it[updatedAt] = now
            }
        }
    }

    private fun generateCode(length: Int): String {
        val max = (1..length).fold(1) { acc, _ -> acc * 10 }
        return Random.nextInt(0, max).toString().padStart(length, '0')
    }
}
