package tech.hanasaki.azusa.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val avatar: String? = null,
)

@Serializable
data class AvatarUploadResponse(
    val success: Boolean,
    val message: String,
    val data: AvatarUploadData,
)

@Serializable
data class AvatarUploadData(
    val avatarUrl: String,
    val profile: ProfileData,
)
