package tech.hanasaki.momotalk_plus.features.settings.domain.model

data class SettingsPreferences(
    val themeId: String = "system",
    val notificationsEnabled: Boolean = false,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
)
