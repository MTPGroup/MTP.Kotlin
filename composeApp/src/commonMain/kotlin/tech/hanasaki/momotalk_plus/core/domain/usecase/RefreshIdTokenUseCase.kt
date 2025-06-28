package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class RefreshIdTokenUseCase(
    private val userRepository: UserRepository,
) {
    /**
     * 刷新用户的 ID 令牌。
     *
     * @return 返回一个 [Result]，成功时包含新的 ID 令牌，失败时包含错误信息。
     */
    suspend operator fun invoke(): Result<RefreshInfo, AppError> =
        userRepository.refreshIdToken().mapError { error ->
            when (error) {
                is UserError.ApiError -> AppError("Failed to fetch user information. Please try again later.")
                is UserError.NetworkError -> AppError("Network error. Please try again later.")
                UserError.Unknown -> AppError("Unknown error")
            }
        }
}