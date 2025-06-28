package tech.hanasaki.momotalk_plus.features.login.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError

class LoginUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<RefreshInfo, AppError> {
        if (email == "" || password == "") {
            return Result.Error(AppError("Email and password cannot be empty"))
        }
        return authRepository.signInWithEmailPassword(email, password).mapError { error ->
            when (error) {
                is AuthError.NetworkError -> AppError("Network error occurred. Please try again.")
                is AuthError.ApiError -> AppError("Invalid email or password.")
                else -> AppError("An unexpected error occurred")
            }
        }
    }
}