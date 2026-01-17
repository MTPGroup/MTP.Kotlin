package tech.hanasaki.azusa.auth.internal.domain.repository

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.User
import tech.hanasaki.azusa.auth.internal.domain.model.UserId


interface UserRepository {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
}
