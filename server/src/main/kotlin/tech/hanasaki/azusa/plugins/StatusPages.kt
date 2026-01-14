package tech.hanasaki.azusa.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.infrastructure.utils.ApiException

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
    val timestamp: String,
)

@Serializable
data class ErrorDetail(
    val message: String,
    val code: String = "INTERNAL_SERVER_ERROR",
    val details: String? = null,
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            val payload = ErrorResponse(
                error = ErrorDetail(
                    message = cause.message ?: "Request Failed",
                    code = cause.code,
                    details = cause.detail,
                ),
                timestamp = Clock.System.now().toString(),
            )
            call.respond(cause.status, payload)
        }
        exception<Throwable> { call, cause ->
            val payload = ErrorResponse(
                error = ErrorDetail(
                    message = cause.message ?: "Internal Server Error",
                    details = cause.stackTraceToString(),
                ),
                timestamp = Clock.System.now().toString(),
            )
            call.respond(HttpStatusCode.InternalServerError, payload)
        }
    }
}
