package tech.hanasaki.momotalk_plus.features.profile.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository

class UploadAvatarUseCase(
    val repository: ProfileRepository,
) {
    suspend operator fun invoke(
        avatar: ImageData,
    ): String = repository.uploadAvatar(avatar)
}