package tech.hanasaki.momotalk_plus.features.settings.data.datasource.mapper

import tech.hanasaki.momotalk_plus.features.settings.data.datasource.local.entity.SettingsEntity
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto.SettingsResponseData
import tech.hanasaki.momotalk_plus.features.settings.domain.model.Language
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserSettings
import tech.hanasaki.momotalk_plus.features.settings.domain.model.UserTheme

object SettingsMapper {
    fun SettingsEntity.toUserSettings(): UserSettings =
        UserSettings(
            theme = when (theme) {
                "light" -> UserTheme.LIGHT
                "dark" -> UserTheme.DARK
                else -> UserTheme.SYSTEM
            },
            language = when (language) {
                "zh-CN" -> Language.CHINESE
                "en-US" -> Language.ENGLISH
                else -> Language.CHINESE
            },
            notificationsEnabled = notificationsEnabled,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            chatBackgroundUrl = chatBackgroundUrl,
            contactBackgroundUrl = contactBackgroundUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    fun UserSettings.toEntity(): SettingsEntity {
        return SettingsEntity(
            id = 1,
            theme = when (theme) {
                UserTheme.LIGHT -> "light"
                UserTheme.DARK -> "dark"
                UserTheme.SYSTEM -> "system"
            },
            language = when (language) {
                Language.CHINESE -> "zh-CN"
                Language.ENGLISH -> "en-US"
            },
            notificationsEnabled = notificationsEnabled,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            chatBackgroundUrl = chatBackgroundUrl ?: "",
            contactBackgroundUrl = contactBackgroundUrl ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun SettingsResponseData.toUserSettings(): UserSettings =
        UserSettings(
            theme = when (theme) {
                "light" -> UserTheme.LIGHT
                "dark" -> UserTheme.DARK
                else -> UserTheme.SYSTEM
            },
            language = when (language) {
                "zh-CN" -> Language.CHINESE
                "en-US" -> Language.ENGLISH
                else -> Language.CHINESE
            },
            notificationsEnabled = notificationsEnabled,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            chatBackgroundUrl = chatBackgroundUrl,
            contactBackgroundUrl = contactBackgroundUrl,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
