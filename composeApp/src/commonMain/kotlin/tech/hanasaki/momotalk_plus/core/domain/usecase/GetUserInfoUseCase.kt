package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class GetUserInfoUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(uid: String): Result<UserProfile, AppError> =
        userRepository.getCurrentUser(uid).mapError { error ->
            when (error) {
                is UserError.ApiError -> AppError("Failed to fetch user information. Please try again later.")
                is UserError.NetworkError -> AppError("Network error. Please try again later.")
                UserError.Unknown -> AppError("Unknown error")
            }
        }
}