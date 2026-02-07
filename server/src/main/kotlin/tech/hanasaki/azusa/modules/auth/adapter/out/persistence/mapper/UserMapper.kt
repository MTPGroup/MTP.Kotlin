package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.UserTable
import tech.hanasaki.azusa.modules.auth.domain.model.HashedPassword
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserProfile
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

object UserMapper {
    fun toDomain(row: ResultRow): User {
        val userProfile = UserProfile(
            userId = UserId(row[ProfileTable.uid]),
            username = Username(row[ProfileTable.username]),
            avatar = row[ProfileTable.avatar]?.let { AvatarUrl(it) },
            createdAt = row[ProfileTable.createdAt],
            updatedAt = row[ProfileTable.updatedAt],
        )

        return User.reconstitute(
            id = UserId(row[UserTable.id]),
            hashedPassword = HashedPassword(row[UserTable.passwordHash]),
            profile = userProfile,
            status = row[UserTable.status],
            role = row[UserTable.role],
            email = Email(row[UserTable.email]),
            emailVerified = row[UserTable.emailVerified],
            bannedUntil = row[UserTable.bannedUntil]
        )
    }

    fun toEntity(user: User, target: UpdateBuilder<*>) {
        target[UserTable.id] = user.id.value
        target[UserTable.email] = user.email!!.value
        target[UserTable.passwordHash] = user.hashedPassword.value
        target[UserTable.status] = user.status
        target[UserTable.role] = user.role
        target[UserTable.emailVerified] = user.isEmailVerified
        target[UserTable.bannedUntil] = user.bannedUntil
    }
}
