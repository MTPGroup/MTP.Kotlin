package tech.hanasaki.momotalk_plus.features.profile.data.repository

import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.network.*
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.ProfileRemoteDataSource
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.UpdateProfileRequest
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val remote: ProfileRemoteDataSource,
    private val errorMapper: NetworkErrorMapper,
) : ProfileRepository {
    override suspend fun uploadAvatar(avatar: ImageData): String {
        val result = callApi(errorMapper) {
            remote.uploadAvatar(avatar)
        }
        return when (result) {
            is AppResult.Success -> result.data.avatar
            is AppResult.Failure -> result.throwAsException()
        }
    }

    override suspend fun updateUserProfile(username: String, avatar: String?) {
        callApi(errorMapper) {
            remote.updateMe(
                UpdateProfileRequest(
                    username = username,
                    avatar = avatar
                )
            )
        }.throwIfFailure()
    }
}
