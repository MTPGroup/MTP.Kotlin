package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
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
    val session: Session,
    val user: UserProfile,
)
