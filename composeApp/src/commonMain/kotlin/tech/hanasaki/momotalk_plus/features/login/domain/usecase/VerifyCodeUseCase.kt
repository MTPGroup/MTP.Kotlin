package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class VerifyCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        verificationId: String,
        verificationCode: String,
    ): Result<String, AppError> {
        if (verificationId.isEmpty() || verificationCode.isEmpty()) {
            return Result.Error(AppError("Verification ID and code cannot be empty."))
        }
        return authRepository.verifyPasswordResetCode(verificationId, verificationCode)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                    is AuthError.ApiError -> AppError("Failed to verify code. It might be invalid or expired.")
                    else -> AppError("An unexpected error occurred")
                }
            }
    }
}