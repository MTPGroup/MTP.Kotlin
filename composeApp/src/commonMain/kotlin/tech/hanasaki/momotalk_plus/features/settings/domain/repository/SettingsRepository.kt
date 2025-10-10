package tech.hanasaki.momotalk_plus.features.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.settings.domain.model.Language
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserTheme

interface SettingsRepository {
    fun observeUserSettings(): Flow<UserSettings>
    suspend fun saveTheme(theme: UserTheme)
    suspend fun saveLanguage(language: Language)
    suspend fun saveNotificationsEnabled(enabled: Boolean)
    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun saveVibrationEnabled(enabled: Boolean)
    suspend fun saveChatBackgroundUrl(url: String?)
    suspend fun saveContactBackgroundUrl(url: String?)
    suspend fun clearAll()
}
