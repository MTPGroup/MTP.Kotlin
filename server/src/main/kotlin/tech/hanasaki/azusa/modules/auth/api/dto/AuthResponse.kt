package tech.hanasaki.azusa.modules.auth.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpResponse(
    val success: Boolean,
)

@Serializable
data class SignInWithPasswordResponse(
    val token: String,
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
data class SendEmailVerificationResponse(
    val success: Boolean,
)

@Serializable
data class SendPasswordResetEmailResponse(
    val success: Boolean,
)

@Serializable
data class VerifyOTPResponse(
    val token: String?,
    val user: UserProfile,
)

@Serializable
data class ResetPasswordResponse(
    val success: Boolean,
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val createdAt: String,
    val updatedAt: String,
)
