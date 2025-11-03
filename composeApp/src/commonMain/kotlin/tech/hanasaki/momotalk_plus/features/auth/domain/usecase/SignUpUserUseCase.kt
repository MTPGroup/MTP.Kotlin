package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignUpUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<Unit> = try {
        repository.signUp(email, password)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}