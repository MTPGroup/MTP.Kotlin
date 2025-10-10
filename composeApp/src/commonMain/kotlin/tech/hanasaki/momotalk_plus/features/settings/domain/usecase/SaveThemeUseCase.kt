package tech.hanasaki.momotalk_plus.features.settings.domain.usecase

import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserTheme
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SaveThemeUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(theme: String): Result<Unit> = try {
        val th = when (theme) {
            "light" -> UserTheme.LIGHT
            "dark" -> UserTheme.DARK
            "system" -> UserTheme.SYSTEM
            else -> UserTheme.SYSTEM
        }
        repository.saveTheme(th)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
