package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class SignUpUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit, AppError> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(AppError("Email and password cannot be empty"))
        }
        return authRepository.signUpWithEmailPassword(email, password).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                is AuthError.ApiError -> AppError("Failed to sign up. Please check your details.")
                else -> AppError("An unexpected error occurred")
            }
        }
    }
}