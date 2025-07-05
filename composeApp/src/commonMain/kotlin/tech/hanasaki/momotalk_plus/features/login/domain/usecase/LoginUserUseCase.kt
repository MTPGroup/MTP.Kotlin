package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import io.ktor.client.plugins.auth.providers.*
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.datasource.local.TokenStorage
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository

class LoginUserUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(username: String, password: String): Result<String, AppError> {
        if (username == "" || password == "") {
            return Result.Error(AppError("用户名或密码不能为空"))
        }
        return authRepository.signInWithPassword(username, password)
            .map { signInWithPasswordResponse ->
                // 保存用户的 ID 令牌到本地存储
                tokenStorage.saveTokens(
                    BearerTokens(
                        signInWithPasswordResponse.accessToken,
                        signInWithPasswordResponse.refreshToken
                    )
                )
                signInWithPasswordResponse.sub
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