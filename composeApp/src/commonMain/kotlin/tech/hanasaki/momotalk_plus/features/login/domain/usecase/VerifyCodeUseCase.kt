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
            return Result.Error(AppError("验证码不能为空"))
        }
        return authRepository.verifyPasswordResetCode(verificationId, verificationCode)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                    is AuthError.ApiError -> AppError("验证码错误或已过期")
                    else -> AppError("未知错误")
                }
            }
    }
}