package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository

/**
 * 登出 UseCase
 *
 * 清除会话信息，包括本地缓存和远程会话
 */
class LogoutUseCase(
    private val repository: SessionRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repository.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

