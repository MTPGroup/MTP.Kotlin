package tech.hanasaki.momotalk_plus.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.domain.model.Language
import tech.hanasaki.momotalk_plus.core.domain.model.ThemeName
import tech.hanasaki.momotalk_plus.core.domain.model.UserSettings

interface SettingsRepository {
    fun observeUserSettings(): Flow<UserSettings>
    suspend fun saveTheme(theme: ThemeName)
    suspend fun saveLanguage(language: Language)
    suspend fun saveNotificationsEnabled(enabled: Boolean)
    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun saveVibrationEnabled(enabled: Boolean)
    suspend fun saveChatBackgroundUrl(url: String?)
    suspend fun saveContactBackgroundUrl(url: String?)
    suspend fun clearAll()
}
