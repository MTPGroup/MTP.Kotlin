package tech.hanasaki.momotalk_plus.core.domain.usecase

import tech.hanasaki.momotalk_plus.core.domain.model.ThemeName
import tech.hanasaki.momotalk_plus.core.domain.repository.SettingsRepository

class SaveThemeUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(theme: String): Result<Unit> = try {
        val th = when (theme) {
            "light" -> ThemeName.LIGHT
            "dark" -> ThemeName.DARK
            "system" -> ThemeName.SYSTEM
            else -> ThemeName.SYSTEM
        }
        repository.saveTheme(th)
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
