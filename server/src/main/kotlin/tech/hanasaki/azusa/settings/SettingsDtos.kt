package tech.hanasaki.azusa.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SettingsResponse(
    val success: Boolean,
    val message: String,
    val data: SettingsData,
)

@Serializable
data class SettingsData(
    @SerialName("ownerId")
    val ownerId: String,
    val theme: String,
    val chatModels: JsonElement,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String,
)

@Serializable
data class UpdateSettingsRequest(
    val theme: String? = null,
    val chatModels: JsonElement? = null,
)
