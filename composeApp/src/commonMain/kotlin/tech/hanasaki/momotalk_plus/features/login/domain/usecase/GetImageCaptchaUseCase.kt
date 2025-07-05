package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.data.model.CaptchaResponse
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class GetImageCaptchaUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<CaptchaResponse, AppError> =
        authRepository.getImageCaptcha()
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                    is AuthError.ApiError -> AppError(error.message)
                    else -> AppError("未知错误")
                }
            }
}