package tech.hanasaki.momotalk_plus.features.settings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.*
import tech.hanasaki.momotalk_plus.db.AppDatabase
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.mapper.SettingsMapper.toEntity
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.mapper.SettingsMapper.toUserSettings
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.api.SettingApi
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.UpdateSettingsRequest
import tech.hanasaki.momotalk_plus.features.settings.domain.model.Language
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserTheme
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository
import kotlin.time.Duration.Companion.hours

class SettingsRepositoryImpl(
    private val settingApi: SettingApi,
    database: AppDatabase,
) : SettingsRepository {
    private val settingsDao = database.settingsDao()

    companion object {
        private val DEFAULT_SETTINGS = UserSettings(
            theme = UserTheme.SYSTEM,
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

    /**
     * 设置 Store
     * 使用 Store5 提供缓存和持久化能力
     * 策略：先返回本地缓存（快速响应），然后从网络获取最新数据并更新
     */
    private val settingsStore: Store<Unit, UserSettings> = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: Unit ->
                // 从服务器获取最新设置
                settingApi.fetchSettings().data.toUserSettings()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { _: Unit ->
                    // 从数据库读取设置，如果为空则使用默认值
                    settingsDao.getSettings().map { entity ->
                        entity?.toUserSettings() ?: DEFAULT_SETTINGS
                    }
                },
                writer = { _: Unit, settings: UserSettings ->
                    // 写入数据库
                    settingsDao.upsert(settings.toEntity())
                },
                delete = { _: Unit ->
                    settingsDao.deleteAll()
                },
                deleteAll = {
                    settingsDao.deleteAll()
                }
            )
        )
        .cachePolicy(
            MemoryPolicy.builder<Unit, UserSettings>()
                .setExpireAfterWrite(24.hours) // 内存缓存 24 小时
                .build()
        )
        .build()

    override fun observeUserSettings(): Flow<UserSettings> {
        // 直接从数据库 Flow 读取，确保响应式更新
        // Room 会自动监听数据库变化并发射新值
        return settingsDao.getSettings()
            .map { entity ->
                entity?.toUserSettings() ?: DEFAULT_SETTINGS
            }
            .onStart {
                // 在开始时尝试从服务器获取最新数据
                try {
                    val remoteSettings = settingApi.fetchSettings().data.toUserSettings()
                    settingsDao.upsert(remoteSettings.toEntity())
                } catch (e: Exception) {
                    // 网络失败时静默处理，使用本地缓存
                    e.printStackTrace()
                }
            }
    }

    override suspend fun saveTheme(theme: UserTheme) {
        updateSettings { currentSettings ->
            val themeValue = when (theme) {
                UserTheme.LIGHT -> "light"
                UserTheme.DARK -> "dark"
                UserTheme.SYSTEM -> "system"
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

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun clearAll() {
        settingsStore.clear()
    }

    /**
     * 辅助方法：更新设置
     * 直接更新数据库，SourceOfTruth 的 reader (Flow) 会自动检测变化并发射新值
     */
    private suspend fun updateSettings(update: suspend (UserSettings) -> UserSettings) {
        // 获取当前设置
        val currentSettings = settingsDao.getSettings().first() ?: DEFAULT_SETTINGS.toEntity()
        val currentUserSettings = currentSettings.toUserSettings()

        // 应用更新
        val newSettings = update(currentUserSettings)

        // 直接写入数据库
        // 由于 SourceOfTruth 的 reader 是 Flow，Room 会自动通知变化
        // Store 会检测到 SourceOfTruth 的变化并发射新值到 stream
        settingsDao.upsert(newSettings.toEntity())
    }

    /**
     * 辅助方法：同步到服务器
     * 采用最佳努力策略：失败时静默处理，不影响用户体验
     */
    private suspend fun syncToServer(request: UpdateSettingsRequest) {
        try {
            settingApi.updateSettings(request)
        } catch (e: Exception) {
            // 静默失败：本地已经更新，服务器同步失败不影响用户使用
            // 可以在这里添加日志记录或重试机制
            e.printStackTrace()
        }
    }
}
