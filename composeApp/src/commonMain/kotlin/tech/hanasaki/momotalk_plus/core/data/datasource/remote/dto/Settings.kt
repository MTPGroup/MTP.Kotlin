package tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponseData(
    val theme: String,
    val language: String? = null,
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean? = null,
    @SerialName("sound_enabled")
    val soundEnabled: Boolean? = null,
    @SerialName("vibration_enabled")
    val vibrationEnabled: Boolean? = null,
    @SerialName("chat_background_url")
    val chatBackgroundUrl: String? = null,
    @SerialName("contact_background_url")
    val contactBackgroundUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
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
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean? = null,
    @SerialName("sound_enabled")
    val soundEnabled: Boolean? = null,
    @SerialName("vibration_enabled")
    val vibrationEnabled: Boolean? = null,
    @SerialName("chat_background_url")
    val chatBackgroundUrl: String? = null,
    @SerialName("contact_background_url")
    val contactBackgroundUrl: String? = null,
)
