package tech.hanasaki.azusa.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val name: String,
    val password: String,
    val callbackURL: String,
)

@Serializable
data class SignUpResponse(
    val token: String?,
    val refreshToken: String,
    val user: UserProfile,
)

@Serializable
data class SignInWithPasswordRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignInWithPasswordResponse(
    val redirect: Boolean,
    val token: String,
    val refreshToken: String,
    val user: UserProfile,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
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
data class SignOutRequest(
    val refreshToken: String? = null,
)

@Serializable
data class SendEmailVerificationRequest(
    val email: String,
    val type: String,
)

@Serializable
data class SendEmailVerificationResponse(
    val success: Boolean,
)

@Serializable
data class SendPasswordResetEmailRequest(
    val email: String,
)

@Serializable
data class SendPasswordResetEmailResponse(
    val success: Boolean,
)

@Serializable
data class VerifyOTPRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class VerifyOTPResponse(
    val token: String?,
    val user: UserProfile,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
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
    val image: String?,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
