package tech.hanasaki.momotalk_plus.core.domain.model

enum class ThemeName(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system")
}

enum class Language(val value: String) {
    ENGLISH("en-US"),
    CHINESE("zh-CN"),
}

data class UserSettings(
    val theme: ThemeName,
    val language: Language,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val chatBackgroundUrl: String?,
    val contactBackgroundUrl: String?,
    val createdAt: String,
    val updatedAt: String,
)

