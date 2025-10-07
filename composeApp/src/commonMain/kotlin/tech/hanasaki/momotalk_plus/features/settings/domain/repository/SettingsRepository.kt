package tech.hanasaki.momotalk_plus.features.settings.domain.repository

import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings

interface SettingsRepository {
    fun getUserSettings(): UserSettings
    fun saveThemeId(themeId: String)
    fun saveNotificationsEnabled(enabled: Boolean)
    fun saveSoundEnabled(enabled: Boolean)
    fun saveVibrationEnabled(enabled: Boolean)
    fun clearAll()
}

