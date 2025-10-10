package tech.hanasaki.momotalk_plus.features.profile.domain.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError

/**
 * ProfileRepository - 个人资料仓库接口
 */
interface ProfileRepository {
    /**
     * 更新用户个人资料
     *
     * @param name 用户名
     * @param image 用户头像URL
     * @return IResult<[Unit], [AppError]> 成功返回更新后的用户信息，失败返回错误
     */
    suspend fun updateUserProfile(
        name: String,
        image: String?,
    )
}

