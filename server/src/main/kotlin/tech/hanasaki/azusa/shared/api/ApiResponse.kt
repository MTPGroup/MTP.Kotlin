package tech.hanasaki.azusa.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: String,
)

@Serializable
data class ErrorDetail(
    val message: String,
    val code: String = "INTERNAL_SERVER_ERROR",
    val details: String? = null,
)
