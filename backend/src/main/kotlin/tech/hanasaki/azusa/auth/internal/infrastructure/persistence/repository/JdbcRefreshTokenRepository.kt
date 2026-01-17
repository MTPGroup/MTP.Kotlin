package tech.hanasaki.azusa.auth.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.auth.domain.model.UserId
import tech.hanasaki.azusa.auth.domain.repository.RefreshTokenRepository
import java.sql.Timestamp
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class JdbcRefreshTokenRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : RefreshTokenRepository {
    override suspend fun save(refreshToken: RefreshToken): Unit = withContext(Dispatchers.IO) {
        val params = MapSqlParameterSource()
            .addValue("id", refreshToken.id)
            .addValue("userId", refreshToken.userId.value)
            .addValue("tokenHash", refreshToken.tokenHash)
            .addValue("expiresAt", Timestamp.from(refreshToken.expiresAt.toJavaInstant()))
            .addValue("isRevoked", refreshToken.isRevoked)
        jdbcTemplate.update(
            """
                insert into public.refresh_tokens (id, user_id, token_hash, expires_at, is_revoked)
                values (:id, :userId, :tokenHash, :expiresAt, :isRevoked)
            """.trimIndent(),
            params
        )
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshToken? = withContext(Dispatchers.IO) {
        val sql = """
            select id, user_id, token_hash, expires_at, is_revoked
              from public.refresh_tokens
             where token_hash = :tokenHash
        """.trimIndent()
        jdbcTemplate.query(sql, mapOf("tokenHash" to tokenHash)) { rs, _ ->
            RefreshToken(
                id = rs.getObject("id", UUID::class.java),
                userId = UserId(rs.getObject("user_id", UUID::class.java)),
                tokenHash = rs.getString("token_hash"),
                expiresAt = rs.getTimestamp("expires_at").toInstant().toKotlinInstant(),
                isRevoked = rs.getBoolean("is_revoked"),
            )
        }.singleOrNull()
    }

    override suspend fun revoke(refreshToken: RefreshToken): Unit = withContext(Dispatchers.IO) {
        jdbcTemplate.update(
            """
                update public.refresh_tokens
                   set is_revoked = true
                 where id = :id
            """.trimIndent(),
            mapOf("id" to refreshToken.id)
        )
    }

    override suspend fun revokeAllForUser(userId: UserId): Unit = withContext(Dispatchers.IO) {
        jdbcTemplate.update(
            """
                update public.refresh_tokens
                   set is_revoked = true
                 where user_id = :userId
            """.trimIndent(),
            mapOf("userId" to userId.value)
        )
    }
}
