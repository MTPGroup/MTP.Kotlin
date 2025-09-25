package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String, newPassword: String): IResult<Unit, AppError> =
        repository.resetPassword(email, otp, newPassword).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                is AuthError.ApiError -> AppError("重置密码失败")
                else -> AppError("未知错误")
            }
        }
}