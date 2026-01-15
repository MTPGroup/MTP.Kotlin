package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import java.sql.Timestamp
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class JdbcOtpRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : OtpRepository {
    override suspend fun save(otp: Otp): Unit = withContext(Dispatchers.IO) {
        val params = MapSqlParameterSource()
            .addValue("id", otp.id)
            .addValue("email", otp.email.value)
            .addValue("code", otp.code)
            .addValue("type", otp.type.name)
            .addValue("expiresAt", Timestamp.from(otp.expiresAt.toJavaInstant()))
            .addValue("isUsed", otp.isUsed)
        jdbcTemplate.update(
            """
                insert into public.otp_codes (id, email, code, type, expires_at, is_used)
                values (:id, :email, :code, :type, :expiresAt, :isUsed)
            """.trimIndent(),
            params
        )
    }

    override suspend fun findValidLatest(email: Email, type: OtpType): Otp? = withContext(Dispatchers.IO) {
        val sql = """
            select id, email, code, type, expires_at, is_used
              from public.otp_codes
             where email = :email and type = :type and is_used = false
             order by created_at desc
             limit 1
        """.trimIndent()
        jdbcTemplate.query(
            sql,
            mapOf("email" to email.value, "type" to type.name)
        ) { rs, _ ->
            Otp(
                id = rs.getObject("id", java.util.UUID::class.java),
                email = Email(rs.getString("email")),
                code = rs.getString("code"),
                type = OtpType.valueOf(rs.getString("type")),
                expiresAt = rs.getTimestamp("expires_at").toInstant().toKotlinInstant(),
                isUsed = rs.getBoolean("is_used"),
            )
        }.singleOrNull()
    }

    override suspend fun markAsUsed(otp: Otp): Unit = withContext(Dispatchers.IO) {
        jdbcTemplate.update(
            """
                update public.otp_codes
                   set is_used = true
                 where id = :id
            """.trimIndent(),
            mapOf("id" to otp.id)
        )
    }
}
