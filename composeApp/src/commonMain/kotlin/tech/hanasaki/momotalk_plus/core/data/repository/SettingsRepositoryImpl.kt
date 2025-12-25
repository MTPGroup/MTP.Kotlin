package tech.hanasaki.momotalk_plus.core.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.SettingsMapper.toEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.SettingsMapper.toUserSettings
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.SettingsResponse
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UpdateSettingsRequest
import tech.hanasaki.momotalk_plus.core.domain.model.Language
import tech.hanasaki.momotalk_plus.core.domain.model.ThemeName
import tech.hanasaki.momotalk_plus.core.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.core.domain.repository.SettingsRepository
import tech.hanasaki.momotalk_plus.db.AppDatabase

class SettingsRepositoryImpl(
    private val supabase: SupabaseClient,
    database: AppDatabase,
) : SettingsRepository {
    private val settingsDao = database.settingsDao()

    companion object {
        private val DEFAULT_SETTINGS = UserSettings(
            theme = ThemeName.SYSTEM,
            language = Language.CHINESE,
            notificationsEnabled = false,
            soundEnabled = false,
            vibrationEnabled = false,
            chatBackgroundUrl = null,
            contactBackgroundUrl = null,
            createdAt = "",
            updatedAt = ""
        )
    }

    override fun observeUserSettings(): Flow<UserSettings> {
        return settingsDao.getSettings()
            .map { entity ->
                entity?.toUserSettings() ?: DEFAULT_SETTINGS
            }
            .onStart {
                try {
                    val response = supabase.functions.invoke("settings") {
                        url { path("settings") }
                        method = HttpMethod.Get
                    }
                    val remoteSettings = response.body<SettingsResponse>().data.toUserSettings()
                    settingsDao.upsert(remoteSettings.toEntity())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    override suspend fun saveTheme(theme: ThemeName) {
        updateSettings { currentSettings ->
            val themeValue = when (theme) {
                ThemeName.LIGHT -> "light"
                ThemeName.DARK -> "dark"
                ThemeName.SYSTEM -> "system"
            }

            syncToServer(
                UpdateSettingsRequest(
                    theme = themeValue,
                )
            )

            currentSettings.copy(theme = theme)
        }
    }

    override suspend fun saveLanguage(language: Language) {
        updateSettings { currentSettings ->
            val languageValue = when (language) {
                Language.CHINESE -> "zh-CN"
                Language.ENGLISH -> "en-US"
            }

            syncToServer(
                UpdateSettingsRequest(
                    language = languageValue,
                )
            )

            currentSettings.copy(language = language)
        }
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean) {
        updateSettings { currentSettings ->
            syncToServer(
                UpdateSettingsRequest(
                    notificationsEnabled = enabled,
                    soundEnabled = if (!enabled) false else null,
                    vibrationEnabled = if (!enabled) false else null
                )
            )

            currentSettings.copy(
                notificationsEnabled = enabled,
                soundEnabled = if (!enabled) false else currentSettings.soundEnabled,
                vibrationEnabled = if (!enabled) false else currentSettings.vibrationEnabled,
            )
        }
    }

    override suspend fun saveSoundEnabled(enabled: Boolean) {
        updateSettings { currentSettings ->
            syncToServer(
                UpdateSettingsRequest(
                    soundEnabled = enabled,
                )
            )

            currentSettings.copy(soundEnabled = enabled)
        }
    }

    override suspend fun saveVibrationEnabled(enabled: Boolean) {
        updateSettings { currentSettings ->
            syncToServer(
                UpdateSettingsRequest(
                    vibrationEnabled = enabled,
                )
            )

            currentSettings.copy(vibrationEnabled = enabled)
        }
    }

    override suspend fun saveChatBackgroundUrl(url: String?) {
        updateSettings { currentSettings ->
            syncToServer(
                UpdateSettingsRequest(
                    chatBackgroundUrl = url,
                )
            )

            currentSettings.copy(chatBackgroundUrl = url)
        }
    }

    override suspend fun saveContactBackgroundUrl(url: String?) {
        updateSettings { currentSettings ->
            syncToServer(
                UpdateSettingsRequest(
                    contactBackgroundUrl = url
                )
            )

            currentSettings.copy(contactBackgroundUrl = url)
        }
    }

    override suspend fun clearAll() {
        settingsDao.deleteAll()
    }

    private suspend fun updateSettings(update: suspend (UserSettings) -> UserSettings) {
        val currentSettings = settingsDao.getSettings().first() ?: DEFAULT_SETTINGS.toEntity()
        val currentUserSettings = currentSettings.toUserSettings()

        val newSettings = update(currentUserSettings)

        settingsDao.upsert(newSettings.toEntity())
    }

    private suspend fun syncToServer(request: UpdateSettingsRequest) {
        try {
            supabase.functions.invoke("settings") {
                url { path("settings") }
                method = HttpMethod.Patch
                setBody(request)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
