package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class SignUpUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String?,
        phoneNumber: String?,
        username: String,
        password: String,
        verificationToken: String,
    ): Result<Unit, AppError> {
        if (email.isNullOrEmpty() && phoneNumber.isNullOrEmpty()) {
            return Result.Error(AppError("邮箱或手机号不能为空"))
        }
        if (username.isBlank()) {
            return Result.Error(AppError("用户名不能为空"))
        }
        if (password.length < 6) {
            return Result.Error(AppError("密码长度不能少于6位"))
        }
        return authRepository.signUp(email, phoneNumber, username, password, verificationToken)
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                    is AuthError.ApiError -> AppError(
                        "注册失败，请检查您的信息",
                    )

                    else -> AppError("未知错误")
                }
            }
    }
}