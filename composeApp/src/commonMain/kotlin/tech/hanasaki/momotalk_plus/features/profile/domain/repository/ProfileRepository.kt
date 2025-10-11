package tech.hanasaki.momotalk_plus.features.profile.domain.repository

/**
 * ProfileRepository - 个人资料仓库接口
 */
interface ProfileRepository {
    /**
     * 更新用户个人资料
     *
     * @param name 用户名
     * @param image 用户头像URL
     */
    suspend fun updateUserProfile(
        name: String,
        image: String?,
    )
}

