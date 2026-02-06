package tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class SuccessResponse(
    val success: Boolean = true,
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
data class LoginResponse(
    val user: UserProfile,
    val tokens: TokenResponse,
)


@Serializable
data class UserProfile(
    val userId: Uuid,
    val email: String,
    val username: String,
    val avatar: String?,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
