package tech.hanasaki.azusa.auth.domain.repository

import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.User
import tech.hanasaki.azusa.common.UserId


interface UserRepository {
    fun findByEmail(email: Email): User?
    fun findById(id: UserId): User?
    fun save(user: User)
    fun deleteById(id: UserId)
}
