package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(oobCode: String, newPassword: String): Result<Unit, AppError> {
        if (oobCode.isBlank()) {
            return Result.Error(AppError("Code cannot be empty"))
        }
        if (newPassword.length < 6) {
            return Result.Error(AppError("Password must be at least 6 characters long"))
        }
        return authRepository.resetPassword(oobCode, newPassword).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                is AuthError.ApiError -> AppError("Failed to reset password. The code might be invalid or expired.")
                else -> AppError("An unexpected error occurred")
            }
        }
    }
}