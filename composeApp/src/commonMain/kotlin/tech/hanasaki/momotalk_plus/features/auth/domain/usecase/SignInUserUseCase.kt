package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignInUserUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<String> =
        try {
            val response = authRepository.signInWithPassword(email, password)

            // 登录成功后，刷新会话信息（这会触发 SessionRepository 从服务器获取并保存会话）
            sessionRepository.refreshCurrentSession()

            Result.success(response.user.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
}