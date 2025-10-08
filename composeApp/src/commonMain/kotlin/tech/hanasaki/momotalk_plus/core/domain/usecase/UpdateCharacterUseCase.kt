package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.Visibility
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class UpdateCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(
        id: String,
        name: String,
        persona: String,
        signature: String,
        avatarUrl: String,
        visibility: Visibility,
    ): IResult<Unit, AppError> =
        repository.updateCharacter(
            id,
            name,
            persona,
            signature,
            avatarUrl,
            visibility
        ).mapError { error ->
            AppError("更新角色时发生错误: $error")
        }
}