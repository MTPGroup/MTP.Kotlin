package tech.hanasaki.momotalk_plus.core.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SessionEntity(
    @PrimaryKey()
    val id: String,
    val token: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String,
    val userAgent: String,
    val userId: String,
)