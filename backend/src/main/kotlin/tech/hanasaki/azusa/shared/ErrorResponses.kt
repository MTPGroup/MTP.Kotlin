package tech.hanasaki.azusa.shared

data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
    val timestamp: String,
)

data class ErrorDetail(
    val message: String,
    val code: String = "INTERNAL_SERVER_ERROR",
    val details: String? = null,
)