package tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val id: String,
    val token: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String,
    val userAgent: String,
    val userId: String,
)

@Serializable
data class GetSessionResponse(
    val session: SessionDto,
    val user: UserProfile,
)
