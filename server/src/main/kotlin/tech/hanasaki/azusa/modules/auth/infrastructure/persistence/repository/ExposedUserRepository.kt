package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import tech.hanasaki.azusa.modules.auth.domain.model.*
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserMetaData
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserTable
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedUserRepository : UserRepository {
    override suspend fun findByEmail(email: Email): User? = dbQuery {
        UserTable.join(ProfileTable, JoinType.INNER, onColumn = UserTable.id, otherColumn = ProfileTable.id)
            .selectAll()
            .where { UserTable.email eq email.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun findById(id: UserId): User? = dbQuery {
        UserTable.join(ProfileTable, JoinType.INNER, onColumn = UserTable.id, otherColumn = ProfileTable.id)
            .selectAll()
            .where { UserTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun save(user: User): Unit = dbQuery {
        val userRowsUpdated = UserTable.update({ UserTable.id eq user.id.value }) {
            it[email] = user.email!!.value
            it[passwordHash] = user.passwordHash.value
            it[rawUserMetaData] = UserMetaData(
                status = user.status,
                emailVerified = user.isEmailVerified,
                bannedUntil = user.bannedUntilAt,
            )
            it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }

        if (userRowsUpdated == 0) {
            UserTable.insert {
                it[id] = user.id.value
                it[email] = user.email!!.value
                it[passwordHash] = user.passwordHash.value
                it[rawUserMetaData] = UserMetaData(
                    status = user.status,
                    emailVerified = user.isEmailVerified,
                    bannedUntil = user.bannedUntilAt,
                )
            }
        }

        val profileRowsUpdated = ProfileTable.update({ ProfileTable.id eq user.id.value }) {
            it[username] = user.profile.username.value
            it[avatar] = user.profile.avatar?.value
            it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }

        if (profileRowsUpdated == 0) {
            ProfileTable.insert {
                it[id] = user.id.value
                it[username] = user.profile.username.value
                it[avatar] = user.profile.avatar?.value
                it[createdAt] = user.profile.createdAt.toLocalDateTime(TimeZone.UTC)
                it[updatedAt] = user.profile.updatedAt.toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    private fun toDomain(row: ResultRow): User {
        val userProfile = UserProfile(
            userId = UserId(row[ProfileTable.id].value),
            username = Username(row[ProfileTable.username]),
            avatar = row[ProfileTable.avatar]?.let { AvatarUrl(it) },
            createdAt = row[ProfileTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[ProfileTable.updatedAt].toInstant(TimeZone.UTC),
        )

        return User(
            id = UserId(row[UserTable.id].value),
            _passwordHash = PasswordHash(row[UserTable.passwordHash]),
            _profile = userProfile,
            _status = row[UserTable.rawUserMetaData].status,
            _email = Email(row[UserTable.email]),
            _emailVerified = row[UserTable.rawUserMetaData].emailVerified,
            _bannedUntil = row[UserTable.rawUserMetaData].bannedUntil
        )
    }
}