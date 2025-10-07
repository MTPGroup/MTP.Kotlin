package tech.hanasaki.momotalk_plus.features.settings.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settings: Settings,
) : SettingsRepository {
    companion object {
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

        private const val DEFAULT_THEME_ID = "default_light"
        private const val DEFAULT_NOTIFICATIONS = true
        private const val DEFAULT_SOUND = true
        private const val DEFAULT_VIBRATION = true
    }

    override fun getUserSettings(): UserSettings {
        return UserSettings(
            themeId = settings.getString(KEY_THEME_ID, DEFAULT_THEME_ID),
            notificationsEnabled = settings.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS),
            soundEnabled = settings.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND),
            vibrationEnabled = settings.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION)
        )
    }

    override fun saveThemeId(themeId: String) {
        settings[KEY_THEME_ID] = themeId
    }

    override fun saveNotificationsEnabled(enabled: Boolean) {
        settings[KEY_NOTIFICATIONS_ENABLED] = enabled
    }

    override fun saveSoundEnabled(enabled: Boolean) {
        settings[KEY_SOUND_ENABLED] = enabled
    }

    override fun saveVibrationEnabled(enabled: Boolean) {
        settings[KEY_VIBRATION_ENABLED] = enabled
    }

    override fun clearAll() {
        settings.clear()
    }
}

