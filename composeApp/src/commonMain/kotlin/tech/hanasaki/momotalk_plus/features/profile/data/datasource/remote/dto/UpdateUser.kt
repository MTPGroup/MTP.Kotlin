package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val avatar: String? = null,
)

@Serializable
data class UpdateUserResponse(
    val status: Boolean,
)

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: ProfileData,
)

@Serializable
data class ProfileData(
    val id: String,
    val uid: String? = null,
    val username: String? = null,
    val avatar: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)
