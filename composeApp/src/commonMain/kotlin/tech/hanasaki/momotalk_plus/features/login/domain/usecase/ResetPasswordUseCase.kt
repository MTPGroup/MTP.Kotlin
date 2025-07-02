package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String?,
        phoneNumber: String?,
        newPassword: String,
        verificationToken: String,
    ): Result<Unit, AppError> {
        if (email.isNullOrEmpty() && phoneNumber.isNullOrEmpty()) {
            return Result.Error(AppError("Either email or phone number must be provided."))
        }
        if (newPassword.length < 6) {
            return Result.Error(AppError("Password must be at least 6 characters long"))
        }
        return authRepository.resetPassword(email, phoneNumber, newPassword, verificationToken)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                    is AuthError.ApiError -> AppError("Failed to reset password. The code might be invalid or expired.")
                    else -> AppError("An unexpected error occurred")
                }
            }
    }
}