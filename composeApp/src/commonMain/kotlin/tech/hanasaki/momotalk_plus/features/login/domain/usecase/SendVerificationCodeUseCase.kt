package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository


class SendVerificationCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String?,
        phoneNumber: String?,
        captchaId: String
    ): Result<String, AppError> {
        if (email.isNullOrEmpty() && phoneNumber.isNullOrEmpty()) {
            return Result.Error(
                AppError(
                    "Please provide either an email or a phone number to reset your password."
                )
            )
        }
        return authRepository.sendResetPasswordCode(email, phoneNumber, captchaId)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError(
                        error.originalException.message ?: "Network error occurred"
                    )

                    is AuthError.ApiError -> AppError(
                        "Failed to reset password. Please check your email."
                    )

                    else -> AppError("An unexpected error occurred")
                }
            }
    }
}