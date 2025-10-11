package tech.hanasaki.momotalk_plus.core.data.datasource.mapper

import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SettingsEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.SettingsResponseData
import tech.hanasaki.momotalk_plus.core.domain.model.Language
import tech.hanasaki.momotalk_plus.core.domain.model.ThemeName
import tech.hanasaki.momotalk_plus.core.domain.model.UserSettings

object SettingsMapper {
    fun SettingsEntity.toUserSettings(): UserSettings =
        UserSettings(
            theme = when (theme) {
                "light" -> ThemeName.LIGHT
                "dark" -> ThemeName.DARK
                else -> ThemeName.SYSTEM
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
                ThemeName.LIGHT -> "light"
                ThemeName.DARK -> "dark"
                ThemeName.SYSTEM -> "system"
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
                "light" -> ThemeName.LIGHT
                "dark" -> ThemeName.DARK
                else -> ThemeName.SYSTEM
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
