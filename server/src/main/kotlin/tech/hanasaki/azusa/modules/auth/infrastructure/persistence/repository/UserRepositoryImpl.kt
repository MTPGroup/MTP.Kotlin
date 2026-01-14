package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.repository.UserRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao.ProfileDao
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.dao.UserDao
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper.UserMapper
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.UserTable

class UserRepositoryImpl : UserRepository {
    override suspend fun findByEmail(email: Email): User? {
        val rawUser = UserDao.find {
            UserTable.email eq email.value
        }.singleOrNull() ?: return null

        val rawProfile = ProfileDao.find {
            ProfileTable.id eq rawUser.id
        }.singleOrNull() ?: error("UserProfile missing for user ${rawUser.id.value}")

        return UserMapper.toDomain(rawUser, rawProfile)
    }

    override suspend fun findById(id: UserId): User? {
        val rawUser = UserDao.findById(id.value) ?: return null
        val rawProfile = ProfileDao.findById(id.value) ?: error("Profile with id ${id.value} not found")

        return UserMapper.toDomain(rawUser, rawProfile)
    }

    override suspend fun save(user: User) {
        val userDao = UserDao.findById(user.id.value)
            ?: UserDao.new(user.id.value) { }
        UserMapper.applyToDao(user, userDao)

        val profileDao = ProfileDao.findById(user.id.value)
            ?: ProfileDao.new(user.id.value) { }
        profileDao.username = user.profile.username.value
        profileDao.avatar = user.profile.avatar?.value
        profileDao.updatedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    }
}