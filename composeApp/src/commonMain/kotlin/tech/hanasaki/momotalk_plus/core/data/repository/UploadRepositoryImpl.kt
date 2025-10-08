package tech.hanasaki.momotalk_plus.core.data.repository

import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UploadRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository

class UploadImageRepositoryImpl(private val remoteDatasource: UploadRemoteDatasource) : UploadImageRepository {
    override suspend fun uploadImage(
        imageData: ImageData,
        path: UploadPath,
        userId: String?,
    ): IResult<String, AppError> =
        remoteDatasource.upload(imageData, path, userId)
}