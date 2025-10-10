package tech.hanasaki.momotalk_plus.features.profile.data.repository

import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.api.ProfileApi
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto.UpdateUserRequest
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

/**
 * ProfileRepositoryImpl - 个人资料仓库实现
 */
class ProfileRepositoryImpl(
    private val profileApi: ProfileApi,
) : ProfileRepository {
    override suspend fun updateUserProfile(
        name: String,
        image: String?,
    ) {
        profileApi.updateUser(
            UpdateUserRequest(
                name = name,
                image = image,
            )
        )
    }
}
