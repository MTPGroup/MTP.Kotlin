package tech.hanasaki.momotalk_plus.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.Character
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository

class ListCharacterUseCase(private val repository: CharacterRepository) {
    operator fun invoke(): Flow<List<Character>> =
        repository.getAvailableCharacters()
}