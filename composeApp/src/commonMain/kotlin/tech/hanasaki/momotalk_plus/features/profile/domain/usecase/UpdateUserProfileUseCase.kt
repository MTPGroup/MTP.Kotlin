package tech.hanasaki.momotalk_plus.features.profile.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

/**
 * UpdateUserProfileUseCase - 更新用户个人资料用例
 */
class UpdateUserProfileUseCase(
    private val repository: ProfileRepository,
) {
    /**
     * 执行更新用户个人资料
     *
     * @param name 用户名
     * @param image 用户头像URL（可选）
     * @return IResult<UserProfile, AppError>
     */
    suspend operator fun invoke(
        name: String,
        image: String? = null,
    ): IResult<Unit, AppError> {
        // 验证用户名不为空
        if (name.isBlank()) {
            return IResult.Error(AppError("用户名不能为空"))
        }

        // 验证用户名长度
        if (name.length !in 2..50) {
            return IResult.Error(AppError("用户名长度应在2-50个字符之间"))
        }

        return repository.updateUserProfile(name, image)
    }
}

