package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

data class SessionEntity(
    val id: String,
    val token: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String,
    val userAgent: String,
    val userId: String,
)
