package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class GetUserInfoUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): IResult<UserProfile?, AppError> =
        userRepository.getCurrentUser().mapError { error ->
            when (error) {
                is UserError.ApiError -> AppError("获取用户信息失败: ${error.message}")
                is UserError.NetworkError -> AppError("网络错误: ${error.originalException.message ?: "无法连接到服务器"}")
                UserError.Unknown -> AppError("未知错误")
            }
        }
}