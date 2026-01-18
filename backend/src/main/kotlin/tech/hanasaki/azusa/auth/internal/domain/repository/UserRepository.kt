package tech.hanasaki.azusa.auth.internal.domain.repository

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.User
import tech.hanasaki.azusa.auth.internal.domain.model.UserId


interface UserRepository {
    fun findByEmail(email: Email): User?
    fun findById(id: UserId): User?
    fun save(user: User)
    fun deleteById(id: UserId)
}
