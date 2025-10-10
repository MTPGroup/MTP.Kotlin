package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

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
