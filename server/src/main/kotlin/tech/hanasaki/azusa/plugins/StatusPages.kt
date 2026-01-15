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
        exception<tech.hanasaki.azusa.shared.domain.exception.AzusaException> { call, cause ->
            val (status, code) = when (cause) {
                is tech.hanasaki.azusa.shared.domain.exception.AuthenticationException -> HttpStatusCode.Unauthorized to "AUTHENTICATION_ERROR"
                is tech.hanasaki.azusa.shared.domain.exception.AuthorizationException -> HttpStatusCode.Forbidden to "AUTHORIZATION_ERROR"
                is tech.hanasaki.azusa.shared.domain.exception.NotFoundException -> HttpStatusCode.NotFound to "NOT_FOUND"
                is tech.hanasaki.azusa.shared.domain.exception.ConflictException -> HttpStatusCode.Conflict to "CONFLICT"
                else -> HttpStatusCode.BadRequest to "DOMAIN_ERROR"
            }
            val payload = ErrorResponse(
                error = ErrorDetail(
                    message = cause.message ?: "Request Failed",
                    code = code,
                    details = cause.stackTraceToString(),
                ),
                timestamp = Clock.System.now().toString(),
            )
            call.respond(status, payload)
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
