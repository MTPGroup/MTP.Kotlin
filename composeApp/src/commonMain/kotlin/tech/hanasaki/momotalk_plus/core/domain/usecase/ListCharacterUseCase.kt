package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class ListCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(): Result<List<Character>, AppError> =
        repository.getAvailableCharacters().mapError { error ->
            AppError("获取角色列表时发生错误: $error")
        }
}