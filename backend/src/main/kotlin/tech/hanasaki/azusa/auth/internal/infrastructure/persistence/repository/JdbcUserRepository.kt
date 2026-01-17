package tech.hanasaki.azusa.auth.infrastructure.persistence.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.auth.domain.model.AvatarUrl
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.auth.domain.model.User
import tech.hanasaki.azusa.auth.domain.model.UserId
import tech.hanasaki.azusa.auth.domain.model.UserProfile
import tech.hanasaki.azusa.auth.domain.model.Username
import tech.hanasaki.azusa.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.auth.infrastructure.persistence.model.UserMetaData
import java.sql.Timestamp
import java.util.*
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class JdbcUserRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : UserRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun findByEmail(email: Email): User? = withContext(Dispatchers.IO) {
        val sql = """
            select u.id as user_id,
                   u.email as user_email,
                   u.password_hash,
                   u.raw_user_meta_data,
                   p.uid as profile_id,
                   p.username,
                   p.avatar,
                   p.created_at as profile_created_at,
                   p.updated_at as profile_updated_at
              from public.users u
              join public.profiles p on p.uid = u.id
             where u.email = :email
        """.trimIndent()
        jdbcTemplate.query(sql, mapOf("email" to email.value)) { rs, _ ->
            toDomain(
                userId = UUID.fromString(rs.getString("user_id")),
                email = rs.getString("user_email"),
                passwordHash = rs.getString("password_hash"),
                rawMeta = rs.getString("raw_user_meta_data"),
                profileId = UUID.fromString(rs.getString("profile_id")),
                username = rs.getString("username"),
                avatar = rs.getString("avatar"),
                profileCreatedAt = rs.getTimestamp("profile_created_at"),
                profileUpdatedAt = rs.getTimestamp("profile_updated_at"),
            )
        }.singleOrNull()
    }

    override suspend fun findById(id: UserId): User? = withContext(Dispatchers.IO) {
        val sql = """
            select u.id as user_id,
                   u.email as user_email,
                   u.password_hash,
                   u.raw_user_meta_data,
                   p.uid as profile_id,
                   p.username,
                   p.avatar,
                   p.created_at as profile_created_at,
                   p.updated_at as profile_updated_at
              from public.users u
              join public.profiles p on p.uid = u.id
             where u.id = :id
        """.trimIndent()
        jdbcTemplate.query(sql, mapOf("id" to id.value)) { rs, _ ->
            toDomain(
                userId = UUID.fromString(rs.getString("user_id")),
                email = rs.getString("user_email"),
                passwordHash = rs.getString("password_hash"),
                rawMeta = rs.getString("raw_user_meta_data"),
                profileId = UUID.fromString(rs.getString("profile_id")),
                username = rs.getString("username"),
                avatar = rs.getString("avatar"),
                profileCreatedAt = rs.getTimestamp("profile_created_at"),
                profileUpdatedAt = rs.getTimestamp("profile_updated_at"),
            )
        }.singleOrNull()
    }

    override suspend fun save(user: User): Unit = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val meta = UserMetaData(
            status = user.status,
            emailVerified = user.isEmailVerified,
            bannedUntil = user.bannedUntilAt,
        )
        val rawMeta = json.encodeToString(UserMetaData.serializer(), meta)

        val userParams = MapSqlParameterSource()
            .addValue("id", user.id.value)
            .addValue("email", user.email!!.value)
            .addValue("passwordHash", user.passwordHash.value)
            .addValue("rawMeta", rawMeta)
            .addValue("updatedAt", Timestamp.from(now.toJavaInstant()))

        val userUpdated = jdbcTemplate.update(
            """
                update public.users
                   set email = :email,
                       password_hash = :passwordHash,
                       raw_user_meta_data = cast(:rawMeta as jsonb),
                       updated_at = :updatedAt
                 where id = :id
            """.trimIndent(),
            userParams
        )

        if (userUpdated == 0) {
            jdbcTemplate.update(
                """
                    insert into public.users (id, email, password_hash, raw_user_meta_data)
                    values (:id, :email, :passwordHash, cast(:rawMeta as jsonb))
                """.trimIndent(),
                userParams
            )
        }

        val profileParams = MapSqlParameterSource()
            .addValue("uid", user.id.value)
            .addValue("username", user.profile.username.value)
            .addValue("avatar", user.profile.avatar?.value)
            .addValue("createdAt", Timestamp.from(user.profile.createdAt.toJavaInstant()))
            .addValue("updatedAt", Timestamp.from(now.toJavaInstant()))

        val profileUpdated = jdbcTemplate.update(
            """
                update public.profiles
                   set username = :username,
                       avatar = :avatar,
                       updated_at = :updatedAt
                 where uid = :uid
            """.trimIndent(),
            profileParams
        )

        if (profileUpdated == 0) {
            jdbcTemplate.update(
                """
                    insert into public.profiles (uid, username, avatar, created_at, updated_at)
                    values (:uid, :username, :avatar, :createdAt, :updatedAt)
                """.trimIndent(),
                profileParams
            )
        }
    }

    private fun toDomain(
        userId: UUID,
        email: String,
        passwordHash: String,
        rawMeta: String,
        profileId: UUID,
        username: String,
        avatar: String?,
        profileCreatedAt: Timestamp,
        profileUpdatedAt: Timestamp,
    ): User {
        val meta = json.decodeFromString(UserMetaData.serializer(), rawMeta)
        val profile = UserProfile(
            userId = UserId(profileId),
            username = Username(username),
            avatar = avatar?.let { AvatarUrl(it) },
            createdAt = profileCreatedAt.toInstant().toKotlinInstant(),
            updatedAt = profileUpdatedAt.toInstant().toKotlinInstant(),
        )
        return User(
            id = UserId(userId),
            _passwordHash = PasswordHash(passwordHash),
            _profile = profile,
            _status = meta.status,
            _email = Email(email),
            _emailVerified = meta.emailVerified,
            _bannedUntil = meta.bannedUntil,
        )
    }

}
