package tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponseData(
    val theme: String,
    val language: String,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val chatBackgroundUrl: String?,
    val contactBackgroundUrl: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SettingsResponse(
    val success: Boolean,
    val message: String,
    val data: SettingsResponseData,
)

@Serializable
data class UpdateSettingsRequest(
    val theme: String? = null,
    val language: String? = null,
    val notificationsEnabled: Boolean? = null,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null,
    val chatBackgroundUrl: String? = null,
    val contactBackgroundUrl: String? = null,
)
