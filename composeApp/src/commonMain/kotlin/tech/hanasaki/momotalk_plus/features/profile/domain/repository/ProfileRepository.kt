package tech.hanasaki.momotalk_plus.features.profile.domain.repository

/**
 * ProfileRepository - 个人资料仓库接口
 */
interface ProfileRepository {
    /**
     * 更新用户个人资料
     *
     * @param id 用户id
     * @param name 用户名
     * @param avatar 用户头像URL
     */
    suspend fun updateUserProfile(
        id: String,
        name: String,
        avatar: String?,
    )
}

