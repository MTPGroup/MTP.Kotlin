package tech.hanasaki.azusa.auth.domain.repository

import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.User
import tech.hanasaki.azusa.auth.domain.model.UserId


interface UserRepository {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
}
