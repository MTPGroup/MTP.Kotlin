package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class SignUpUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String?,
        phoneNumber: String?,
        username: String,
        password: String,
        verificationToken: String,
    ): Result<Unit, AppError> {
        if (email.isNullOrEmpty() && phoneNumber.isNullOrEmpty()) {
            return Result.Error(AppError("Email or phone number must be provided"))
        }
        if (username.isBlank()) {
            return Result.Error(AppError("Username cannot be empty"))
        }
        if (password.length < 6) {
            return Result.Error(AppError("Password must be at least 6 characters long"))
        }
        return authRepository.signUp(email, phoneNumber, username, password, verificationToken)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                    is AuthError.ApiError -> AppError("Failed to sign up. Please check your details.")
                    else -> AppError("An unexpected error occurred")
                }
            }
    }
}