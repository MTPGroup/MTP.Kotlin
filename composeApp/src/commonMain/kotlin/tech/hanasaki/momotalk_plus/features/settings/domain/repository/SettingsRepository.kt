package tech.hanasaki.momotalk_plus.features.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.settings.domain.model.SettingsPreferences

interface SettingsRepository {
    fun observeSettings(): Flow<SettingsPreferences>
    suspend fun saveTheme(themeId: String)
    suspend fun saveNotificationsEnabled(enabled: Boolean)
    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun saveVibrationEnabled(enabled: Boolean)
}
