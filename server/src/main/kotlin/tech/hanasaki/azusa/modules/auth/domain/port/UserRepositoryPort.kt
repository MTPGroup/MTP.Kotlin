package tech.hanasaki.azusa.modules.auth.domain.port

import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.modules.auth.domain.model.User

/**
 * 用户仓储端口 - 被驱动端口（输出端口）
 */
interface UserRepositoryPort {
    suspend fun findByEmail(email: Email): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User)
    suspend fun deleteById(id: UserId)
}