package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository

class UploadImageUseCase(
    private val repository: UploadImageRepository,
) {
    suspend operator fun invoke(
        imagePath: ImageData,
        uploadPath: UploadPath,
        userId: String? = null,
    ): IResult<String, AppError> =
        repository.uploadImage(
            imagePath,
            uploadPath,
            userId
        )
}