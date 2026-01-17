package tech.hanasaki.azusa.shared

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String,
    val data: T? = null,
    val timestamp: String,
)