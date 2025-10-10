package tech.hanasaki.momotalk_plus.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class CharacterDetailUseCase(private val repository: CharacterRepository) {
    operator fun invoke(id: String): Flow<Character?> =
        repository.getCharacterById(id)
            .catch { emit(null) }
}