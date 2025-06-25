package tech.hanasaki.momotalk_plus.features.login.domain.usecase

class SendResetPasswordEmailUseCase(private val authRepository: tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository) {
    suspend operator fun invoke(email: String): tech.hanasaki.momotalk_plus.core.common.Result<Unit, tech.hanasaki.momotalk_plus.core.common.AppError> {
        if (email.isBlank()) {
            return tech.hanasaki.momotalk_plus.core.common.Result.Error(tech.hanasaki.momotalk_plus.core.common.AppError("Email cannot be empty"))
        }
        return authRepository.sendResetPasswordEmail(email).mapError { error ->
            when (error) {
                is tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError.NetworkError -> tech.hanasaki.momotalk_plus.core.common.AppError("Network error occurred. Please try again.")
                is tech.hanasaki.momotalk_plus.features.login.domain.model.AuthError.ApiError -> tech.hanasaki.momotalk_plus.core.common.AppError("Failed to reset password. Please check your email.")
                else -> tech.hanasaki.momotalk_plus.core.common.AppError("An unexpected error occurred")
            }
        }
    }
}