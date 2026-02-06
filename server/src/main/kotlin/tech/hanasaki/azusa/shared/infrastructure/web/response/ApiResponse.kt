package tech.hanasaki.azusa.shared.infrastructure.web.response

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val code: String? = null,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null,
    val timestamp: Instant = Clock.System.now(),
) {
    companion object {
        fun <T> ok(
            data: T,
            message: String = "操作成功",
        ): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> error(
            message: String,
            code: String = "ERROR",
            errors: Map<String, String>? = null,
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                code = code,
                message = message,
                errors = errors
            )
        }
    }
}
