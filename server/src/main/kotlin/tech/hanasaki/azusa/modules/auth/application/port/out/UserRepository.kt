package tech.hanasaki.azusa.modules.auth.application.port.out

import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.User

/**
 * 用户仓储端口 - 被驱动端口（输出端口）
 */
interface UserRepository {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
    suspend fun deleteById(id: UserId)
}