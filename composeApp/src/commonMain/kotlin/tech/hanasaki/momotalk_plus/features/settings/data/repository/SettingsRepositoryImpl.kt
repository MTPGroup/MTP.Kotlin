package tech.hanasaki.momotalk_plus.features.settings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SettingsEntity
import tech.hanasaki.momotalk_plus.core.network.AppErrorException
import tech.hanasaki.momotalk_plus.core.network.AppResult
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.callApi
import tech.hanasaki.momotalk_plus.db.AppDatabase
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.SettingsRemoteDataSource
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.RemoteAppTheme
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.SettingResponseData
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.UpdateSettingRequest
import tech.hanasaki.momotalk_plus.features.settings.domain.model.SettingsPreferences
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val remote: SettingsRemoteDataSource,
    private val errorMapper: NetworkErrorMapper,
    database: AppDatabase,
) : SettingsRepository {

    private val settingsDao = database.settingsDao()
    private var remoteCache: SettingResponseData? = null

    override fun observeSettings(): Flow<SettingsPreferences> =
        settingsDao.getSettings()
            .map { it?.toDomain() ?: SettingsPreferences() }
            .onStart { runCatching { refreshFromServer() } }

    override suspend fun saveTheme(themeId: String) {
        val snapshot = ensureRemoteCache()
        val request = UpdateSettingRequest(
            theme = themeId.toRemoteTheme(),
            llmConfigs = snapshot?.llmConfigs ?: emptySet(),
            activeThemeId = snapshot?.activeThemeId,
            activeLlmConfigId = snapshot?.activeLlmConfigId,
        )

        when (val result = callApi(errorMapper) { remote.updateSettings(request) }) {
            is AppResult.Success -> {
                remoteCache = result.data
                updateLocal { current -> current.copy(themeId = themeId) }
            }

            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    override suspend fun saveNotificationsEnabled(enabled: Boolean) {
        updateLocal { it.copy(notificationsEnabled = enabled) }
    }

    override suspend fun saveSoundEnabled(enabled: Boolean) {
        updateLocal { it.copy(soundEnabled = enabled) }
    }

    override suspend fun saveVibrationEnabled(enabled: Boolean) {
        updateLocal { it.copy(vibrationEnabled = enabled) }
    }

    private suspend fun refreshFromServer() {
        when (val result = callApi(errorMapper) { remote.getSettings() }) {
            is AppResult.Success -> {
                remoteCache = result.data
                updateLocal {
                    it.copy(
                        themeId = result.data.theme.toThemeId(),
                        createdAt = result.data.createdAt,
                        updatedAt = result.data.updatedAt,
                    )
                }
            }

            is AppResult.Failure -> throw AppErrorException(result.error)
        }
    }

    private suspend fun ensureRemoteCache(): SettingResponseData? {
        remoteCache?.let { return it }
        runCatching { refreshFromServer() }
        return remoteCache
    }

    private suspend fun updateLocal(transform: (SettingsPreferences) -> SettingsPreferences) {
        val current = settingsDao.getSettings().first()?.toDomain() ?: SettingsPreferences()
        val next = transform(current)
        settingsDao.upsert(next.toEntity())
    }
}

private fun SettingsEntity.toDomain(): SettingsPreferences = SettingsPreferences(
    themeId = theme,
    notificationsEnabled = notificationsEnabled,
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun SettingsPreferences.toEntity(): SettingsEntity = SettingsEntity(
    id = 1,
    theme = themeId,
    language = "zh-CN",
    notificationsEnabled = notificationsEnabled,
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    chatBackgroundUrl = null,
    contactBackgroundUrl = null,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun String.toRemoteTheme(): RemoteAppTheme = when (lowercase()) {
    "light" -> RemoteAppTheme.LIGHT
    "dark" -> RemoteAppTheme.DARK
    else -> RemoteAppTheme.SYSTEM
}

private fun RemoteAppTheme.toThemeId(): String = when (this) {
    RemoteAppTheme.SYSTEM -> "system"
    RemoteAppTheme.LIGHT -> "light"
    RemoteAppTheme.DARK -> "dark"
}
