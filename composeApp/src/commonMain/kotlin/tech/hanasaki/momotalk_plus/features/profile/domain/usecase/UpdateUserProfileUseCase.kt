package tech.hanasaki.momotalk_plus.features.profile.domain.usecase

import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

class UpdateUserProfileUseCase(
    private val repository: ProfileRepository,
) {
    /**
     * 更新用户个人资料
     *
     * @param username 用户名
     * @param avatar 用户头像URL（可选）
     */
    suspend operator fun invoke(
        username: String,
        avatar: String? = null,
    ) {
        if (username.isBlank()) throw IllegalArgumentException("用户名不能为空")
        if (username.length !in 2..50) throw IllegalArgumentException("用户名应在2-50个字符之间")
        repository.updateUserProfile(username, avatar)
    }
}

