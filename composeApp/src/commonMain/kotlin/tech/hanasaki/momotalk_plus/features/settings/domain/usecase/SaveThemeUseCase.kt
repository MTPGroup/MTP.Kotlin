package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SaveThemeUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(themeId: String): Result<Unit> = try {
        repository.saveTheme(themeId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
