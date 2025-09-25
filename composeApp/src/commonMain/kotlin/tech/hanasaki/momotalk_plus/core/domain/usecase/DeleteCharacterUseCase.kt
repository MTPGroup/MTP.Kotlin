package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository


class DeleteCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(id: String): IResult<Unit, AppError> =
        repository.deleteCharacter(id).mapError { error ->
            AppError("删除角色时发生错误: $error")
        }
}