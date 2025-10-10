package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignOutUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = try {
        repository.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}