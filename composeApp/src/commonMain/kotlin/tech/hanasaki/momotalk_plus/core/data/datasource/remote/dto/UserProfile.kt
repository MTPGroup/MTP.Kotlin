package tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto

import kotlinx.serialization.Serializable

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