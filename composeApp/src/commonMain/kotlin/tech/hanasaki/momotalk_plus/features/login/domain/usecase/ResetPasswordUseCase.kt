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
            return Result.Error(AppError("邮箱或手机号不能为空"))
        }
        if (newPassword.length < 6) {
            return Result.Error(AppError("密码长度不能少于6位"))
        }
        return authRepository.resetPassword(email, phoneNumber, newPassword, verificationToken)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError("网络错误: ${error.originalException.message ?: "请检查网络连接"}")
                    is AuthError.ApiError -> AppError("重置密码失败，验证码可能无效或已过期")
                    else -> AppError("未知错误")
                }
            }
    }
}