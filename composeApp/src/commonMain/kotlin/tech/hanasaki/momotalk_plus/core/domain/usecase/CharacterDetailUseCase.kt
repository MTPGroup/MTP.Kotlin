package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class CharacterDetailUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(id: String): Result<Character?, AppError> =
        repository.getCharacterById(id).mapError { error ->
            AppError("获取角色详情时发生错误: $error")
        }
}