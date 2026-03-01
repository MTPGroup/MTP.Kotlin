package tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInWithPasswordRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SignUpRequest(
    val email: String,
    val name: String,
    val password: String,
)

@Serializable
data class SendOtpRequest(
    val email: String,
    val type: String,
)

@Serializable
data class VerifyOTPRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class SuccessResponse(
    val success: Boolean = true,
)

@Serializable
data class LoginResponse(
    val user: UserProfile,
    val tokens: TokenResponse,
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiredIn: Long,
    val refreshTokenExpiredIn: Long,
    val tokenType: String = "Bearer",
)

@Serializable
data class UserProfile(
    val userId: String,
    val email: String,
    val username: String,
    val avatar: String? = null,
    val isEmailVerified: Boolean,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String,
)
