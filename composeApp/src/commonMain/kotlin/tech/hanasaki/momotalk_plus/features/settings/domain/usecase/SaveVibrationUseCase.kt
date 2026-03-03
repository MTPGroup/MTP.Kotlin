package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SaveVibrationUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = try {
        repository.saveVibrationEnabled(enabled)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
