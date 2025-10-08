package tech.hanasaki.momotalk_plus.features.profile.data.repository

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.ProfileRemoteDataSource
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

/**
 * ProfileRepositoryImpl - 个人资料仓库实现
 */
class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource,
) : ProfileRepository {

    override suspend fun updateUserProfile(
        name: String,
        image: String?,
    ): IResult<Unit, AppError> =
        remoteDataSource.updateUser(name, image).map { }
}
