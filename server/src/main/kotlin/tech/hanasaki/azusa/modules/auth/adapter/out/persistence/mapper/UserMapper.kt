package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.common.domain.model.AvatarUrl
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.UserTable
import tech.hanasaki.azusa.modules.auth.domain.model.PasswordHash
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserProfile
import tech.hanasaki.azusa.modules.auth.domain.model.Username

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
            passwordHash = PasswordHash(row[UserTable.passwordHash]),
            profile = userProfile,
            status = row[UserTable.status],
            email = Email(row[UserTable.email]),
            emailVerified = row[UserTable.emailVerified],
            bannedUntil = row[UserTable.bannedUntil]
        )
    }

    fun toEntity(user: User, target: UpdateBuilder<*>) {
        target[UserTable.id] = user.id.value
        target[UserTable.email] = user.email!!.value
        target[UserTable.passwordHash] = user.passwordHash.value
        target[UserTable.status] = user.status
        target[UserTable.emailVerified] = user.emailVerified
        target[UserTable.bannedUntil] = user.bannedUntil
    }
}
