package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SendPasswordResetEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> = try {
        repository.sendPasswordResetEmail(email)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}