package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.repository.SettingsRepository

class SaveSoundSettingsUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = try {
        repository.saveSoundEnabled(enabled)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
