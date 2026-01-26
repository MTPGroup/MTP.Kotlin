package tech.hanasaki.azusa.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import tech.hanasaki.azusa.common.platform.api.ApiException
import tech.hanasaki.azusa.common.platform.api.ApiResponse
import tech.hanasaki.azusa.common.platform.api.ErrorDetail
import tech.hanasaki.azusa.common.kernel.exception.*
import tech.hanasaki.azusa.common.kernel.exception.NotFoundException
import kotlin.time.Clock

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val payload = ApiResponse<Nothing>(
                success = false,
                message = "Invalid request body",
                error = ErrorDetail(
                    message = "Invalid request body",
                    code = "BAD_REQUEST",
                    details = cause.cause?.message ?: cause.message,
                ),
                timestamp = Clock.System.now(),
            )
            call.respond(HttpStatusCode.BadRequest, payload)
        }
        exception<ContentTransformationException> { call, cause ->
            val payload = ApiResponse<Nothing>(
                success = false,
                message = "Invalid request body",
                error = ErrorDetail(
                    message = "Invalid request body",
                    code = "BAD_REQUEST",
                    details = cause.cause?.message ?: cause.message,
                ),
                timestamp = Clock.System.now(),
            )
            call.respond(HttpStatusCode.BadRequest, payload)
        }
        exception<ApiException> { call, cause ->
            val payload = ApiResponse<Nothing>(
                success = false,
                message = cause.message ?: "Request Failed",
                error = ErrorDetail(
                    message = cause.message ?: "Request Failed",
                    code = cause.code,
                    details = cause.detail,
                ),
                timestamp = Clock.System.now(),
            )
            call.respond(cause.status, payload)
        }
        exception<AzusaException> { call, cause ->
            val (status, code) = when (cause) {
                is AuthenticationException -> HttpStatusCode.Unauthorized to "AUTHENTICATION_ERROR"
                is AuthorizationException -> HttpStatusCode.Forbidden to "AUTHORIZATION_ERROR"
                is NotFoundException -> HttpStatusCode.NotFound to "NOT_FOUND"
                is ConflictException -> HttpStatusCode.Conflict to "CONFLICT"
                else -> HttpStatusCode.BadRequest to "DOMAIN_ERROR"
            }
            val payload = ApiResponse<Nothing>(
                success = false,
                message = cause.message,
                error = ErrorDetail(
                    message = cause.message,
                    code = code,
                    details = cause.stackTraceToString(),
                ),
                timestamp = Clock.System.now(),
            )
            call.respond(status, payload)
        }
        exception<Throwable> { call, cause ->
            val payload = ApiResponse<Nothing>(
                success = false,
                message = cause.message ?: "Internal Server Error",
                error = ErrorDetail(
                    message = cause.message ?: "Internal Server Error",
                    details = cause.stackTraceToString(),
                ),
                timestamp = Clock.System.now(),
            )
            call.respond(HttpStatusCode.InternalServerError, payload)
        }
    }
}
