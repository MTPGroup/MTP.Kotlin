package tech.hanasaki.momotalk_plus.core.domain.usecase

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
    ): Result<String> = try {
        val response = repository.uploadImage(
            imagePath,
            uploadPath,
            userId
        )
        Result.success(response)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}