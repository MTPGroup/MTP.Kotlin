package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import tech.hanasaki.azusa.modules.auth.domain.model.*
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao.ProfileDao
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao.UserDao
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserMetaData

object UserMapper {
    fun toDomain(userDao: UserDao, profileDao: ProfileDao): User {
        val userProfile = UserProfile(
            UserId(profileDao.id.value),
            Username(profileDao.username),
            profileDao.avatar?.let { AvatarUrl(it) },
            profileDao.createdAt.toInstant(TimeZone.UTC),
            profileDao.updatedAt.toInstant(TimeZone.UTC),
        )

        return User(
            id = UserId(userDao.id.value),
            _passwordHash = PasswordHash(userDao.passwordHash),
            _profile = userProfile,
            _status = userDao.rawUserMetaData.status,
            _email = Email(userDao.email),
            _emailVerified = userDao.rawUserMetaData.emailVerified,
            _bannedUntil = userDao.rawUserMetaData.bannedUntil
        )
    }

    fun applyToDao(user: User, userDao: UserDao) {
        userDao.email = user.email?.value ?: ""
        userDao.passwordHash = user.passwordHash.value
        userDao.rawUserMetaData = UserMetaData(
            status = user.status,
            emailVerified = user.isEmailVerified,
            bannedUntil = user.bannedUntilAt,
        )
        userDao.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    }
}