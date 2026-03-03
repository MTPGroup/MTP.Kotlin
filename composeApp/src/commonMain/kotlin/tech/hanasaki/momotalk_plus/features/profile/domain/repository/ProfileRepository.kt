package tech.hanasaki.momotalk_plus.features.profile.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

/**
 * ProfileRepository - 个人资料仓库接口
 */
interface ProfileRepository {
    /**
     * 上传头像
     */
    suspend fun uploadAvatar(avatar: ImageData): String

    /**
     * 更新用户个人资料
     *
     * @param username 用户名
     * @param avatar 用户头像URL
     */
    suspend fun updateUserProfile(
        username: String,
        avatar: String?,
    )
}

