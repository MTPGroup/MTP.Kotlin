package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote

import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
data class SuccessResponse(
    val success: Boolean,
)

@Serializable
data class UpdateProfileRequest(
    val username: String,
    val avatar: String? = null,
)

@Serializable
data class GetProfileResponse(
    val userId: String,
    val email: String,
    val username: String,
    val avatar: String? = null,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class UploadAvatarResponse(
    val avatar: String,
)