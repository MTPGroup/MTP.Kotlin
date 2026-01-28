package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.common.kernel.model.AvatarUrl
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserProfile
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserTable

object UserMapper {
    fun toDomain(row: ResultRow): User {
        val userProfile = UserProfile(
            userId = UserId(row[ProfileTable.uid]),
            username = Username(row[ProfileTable.username]),
            avatar = row[ProfileTable.avatar]?.let { AvatarUrl(it) },
            createdAt = row[ProfileTable.createdAt],
            updatedAt = row[ProfileTable.updatedAt],
        )

        return User(
            id = UserId(row[UserTable.id]),
            _passwordHash = PasswordHash(row[UserTable.passwordHash]),
            _profile = userProfile,
            _status = row[UserTable.status],
            _email = Email(row[UserTable.email]),
            _emailVerified = row[UserTable.emailVerified],
            _bannedUntil = row[UserTable.bannedUntil]
        )
    }

    fun toEntity(user: User, target: UpdateBuilder<*>) {
        target[UserTable.id] = user.id.value
        target[UserTable.email] = user.email!!.value
        target[UserTable.passwordHash] = user.passwordHash.value
        target[UserTable.status] = user.status
        target[UserTable.emailVerified] = user.isEmailVerified
        target[UserTable.bannedUntil] = user.bannedUntilAt
    }
}
