package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignUpUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        username: String,
        password: String,
    ): Result<Unit, AppError> {
        if (email.isBlank()) {
            return Result.Error(AppError("邮箱不能为空"))
        }
        if (username.isBlank()) {
            return Result.Error(AppError("用户名不能为空"))
        }
        if (password.length < 8) {
            return Result.Error(AppError("密码长度不能少于8位"))
        }
        return authRepository.signUp(email, username, password)
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