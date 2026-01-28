package tech.hanasaki.azusa.modules.auth.domain.repository

import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.User


interface UserRepository {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
    suspend fun deleteById(id: UserId)
}
