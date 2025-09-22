package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class VerifyEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit, AppError> =
        repository.verifyEmail(email, otp).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                is AuthError.ApiError -> AppError("验证邮箱失败")
                else -> AppError("未知错误")
            }
        }
}