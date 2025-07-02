package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class VerifyCaptchaUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(captchaId: String, captchaCode: String): Result<String, AppError> =
        authRepository.verifyImageCaptcha(captchaId, captchaCode).map {
            it.captchaToken
        }.mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                is AuthError.ApiError -> AppError("Invalid email or password.")
                else -> AppError("An unexpected error occurred")
            }
        }
}