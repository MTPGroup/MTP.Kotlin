package tech.hanasaki.azusa.modules.auth.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResponse(
    val success: Boolean,
)

@Serializable
data class SignInWithPasswordResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile,
    val expiresIn: Long,
)

@Serializable
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String,
)

@Serializable
data class SignOutResponse(
    val success: Boolean,
)

@Serializable
data class OtpSendResponse(
    val success: Boolean,
)

@Serializable
data class VerifyOTPResponse(
    val success: Boolean,
)

@Serializable
data class ResetPasswordResponse(
    val success: Boolean,
)

@Serializable
data class ChangePasswordResponse(
    val success: Boolean,
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val isEmailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
