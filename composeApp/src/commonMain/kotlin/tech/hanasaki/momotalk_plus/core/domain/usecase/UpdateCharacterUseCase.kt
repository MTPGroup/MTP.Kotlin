package tech.hanasaki.momotalk_plus.core.domain.usecase

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
    ): Result<Unit> = try {
        repository.updateCharacter(
            id,
            name,
            persona,
            signature,
            avatarUrl,
            visibility
        )
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}