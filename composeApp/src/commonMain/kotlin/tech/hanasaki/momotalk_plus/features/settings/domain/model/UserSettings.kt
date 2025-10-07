package tech.hanasaki.momotalk_plus.features.settings.domain.model

data class UserSettings(
    val themeId: String,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
)

