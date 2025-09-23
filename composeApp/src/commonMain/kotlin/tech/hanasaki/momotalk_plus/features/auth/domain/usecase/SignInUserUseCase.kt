package tech.hanasaki.momotalk_plus.features.auth.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.features.auth.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class SignInUserUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<String, AppError> {
        if (email == "" || password == "") {
            return Result.Error(AppError("邮箱或密码不能为空"))
        }
        return authRepository.signInWithPassword(email, password)
            .map { signInWithPasswordResponse ->
                signInWithPasswordResponse.user.id
            }
            .mapError { error ->
                when (error) {
                    is AuthError.NetworkError -> AppError(error.originalException.message ?: "网络错误")
                    is AuthError.ApiError -> AppError("用户名或密码错误")
                    else -> AppError("未知错误")
                }
            }
    }
}