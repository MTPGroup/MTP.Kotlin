package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class VerifyPasswordResetCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(oobCode: String): Result<Unit, AppError> {
        if (oobCode.isBlank()) {
            return Result.Error(AppError("Code cannot be empty"))
        }
        return authRepository.verifyPasswordResetCode(oobCode).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                is AuthError.ApiError -> AppError("Failed to verify code. It might be invalid or expired.")
                else -> AppError("An unexpected error occurred")
            }
        }
    }
}