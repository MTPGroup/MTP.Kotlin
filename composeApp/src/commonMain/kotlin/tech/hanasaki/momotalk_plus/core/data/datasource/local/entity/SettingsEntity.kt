package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

data class SettingsEntity(
    val id: Int = 1,
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val chatBackgroundUrl: String? = null,
    val contactBackgroundUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
