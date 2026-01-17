package tech.hanasaki.azusa.auth.internal.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.internal.domain.model.*
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.UserEntity
import tech.hanasaki.azusa.auth.internal.infrastructure.persistence.entity.UserProfileEntity
import java.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class UserEntityMapper {

    fun toEntity(user: User, now: Instant): UserEntity {
        val profile = user.profile
        val profileEntity = UserProfileEntity(
            userId = user.id.value,
            username = profile.username.value,
            avatar = profile.avatar?.value,
            createdAt = profile.createdAt.toJavaInstant(),
            updatedAt = now,
        )
        return UserEntity(
            id = user.id.value,
            email = user.email?.value,
            passwordHash = user.passwordHash.value,
            status = user.status,
            emailVerified = user.isEmailVerified,
            bannedUntil = user.bannedUntilAt?.toJavaInstant(),
            createdAt = profile.createdAt.toJavaInstant(),
            updatedAt = now,
            profile = profileEntity,
        )
    }

    fun toDomain(entity: UserEntity): User {
        val profileEntity = entity.profile
        val profile = UserProfile(
            userId = UserId(profileEntity.userId),
            username = Username(profileEntity.username),
            avatar = profileEntity.avatar?.let { AvatarUrl(it) },
            createdAt = profileEntity.createdAt.toKotlinInstant(),
            updatedAt = profileEntity.updatedAt.toKotlinInstant(),
        )
        return User(
            id = UserId(entity.id),
            _passwordHash = PasswordHash(entity.passwordHash),
            _profile = profile,
            _status = entity.status,
            _email = entity.email?.let { Email(it) },
            _emailVerified = entity.emailVerified,
            _bannedUntil = entity.bannedUntil?.toKotlinInstant(),
        )
    }
}
