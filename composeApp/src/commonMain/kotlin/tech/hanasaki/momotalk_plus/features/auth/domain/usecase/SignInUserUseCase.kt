package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignInUserUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        try {
            authRepository.signInWithPassword(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
}