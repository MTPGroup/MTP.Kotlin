package tech.hanasaki.azusa.shared.api

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String,
    val data: T? = null,
    val error: ErrorDetail? = null,
    @Contextual val timestamp: Instant,
)

@Serializable
data class ErrorDetail(
    val message: String,
    val code: String = "INTERNAL_SERVER_ERROR",
    val details: String? = null,
)
