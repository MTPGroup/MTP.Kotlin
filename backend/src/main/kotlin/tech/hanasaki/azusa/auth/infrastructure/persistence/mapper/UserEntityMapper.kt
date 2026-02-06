package tech.hanasaki.azusa.auth.infrastructure.persistence.mapper

import org.springframework.stereotype.Component
import tech.hanasaki.azusa.auth.domain.model.*
import tech.hanasaki.azusa.auth.infrastructure.persistence.entity.UserEntity
import tech.hanasaki.azusa.auth.infrastructure.persistence.entity.UserProfileEntity
import tech.hanasaki.azusa.shared.UserId
import java.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class UserEntityMapper {
    fun toEntity(domain: User, now: Instant, isNewRecord: Boolean = false): UserEntity {
        val profile = domain.profile
        val profileEntity = UserProfileEntity(
            uid = domain.id.value,
            username = profile.username.value,
            avatar = profile.avatar?.value,
            createdAt = profile.createdAt.toJavaInstant(),
            updatedAt = now,
        )
        return UserEntity(
            id = domain.id.value,
            email = domain.email?.value,
            passwordHash = domain.passwordHash.value,
            status = domain.status,
            emailVerified = domain.isEmailVerified,
            bannedUntil = domain.bannedUntilAt?.toJavaInstant(),
            createdAt = profile.createdAt.toJavaInstant(),
            updatedAt = now,
            profile = profileEntity,
        ).apply {
            this.isNewRecord = isNewRecord
        }
    }

    fun toDomain(entity: UserEntity): User {
        val profileEntity = entity.profile
        val profile = UserProfile(
            userId = UserId(profileEntity.uid),
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
