package tech.hanasaki.azusa.shared.infrastructure.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tech.hanasaki.azusa.shared.domain.exception.*
import tech.hanasaki.azusa.shared.infrastructure.utils.ApiException
import kotlin.time.Clock

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> {
        val payload = ErrorResponse(
            error = ErrorDetail(
                message = ex.message ?: "Request Failed",
                code = ex.code,
                details = ex.detail,
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, ex.status)
    }

    @ExceptionHandler(AzusaException::class)
    fun handleDomainException(ex: AzusaException): ResponseEntity<ErrorResponse> {
        val (status, code) = when (ex) {
            is AuthenticationException -> HttpStatus.UNAUTHORIZED to "AUTHENTICATION_ERROR"
            is AuthorizationException -> HttpStatus.FORBIDDEN to "AUTHORIZATION_ERROR"
            is NotFoundException -> HttpStatus.NOT_FOUND to "NOT_FOUND"
            is ConflictException -> HttpStatus.CONFLICT to "CONFLICT"
            else -> HttpStatus.BAD_REQUEST to "DOMAIN_ERROR"
        }
        val payload = ErrorResponse(
            error = ErrorDetail(
                message = ex.message ?: "Request Failed",
                code = code,
                details = ex.stackTraceToString(),
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, status)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(ex: Throwable): ResponseEntity<ErrorResponse> {
        val payload = ErrorResponse(
            error = ErrorDetail(
                message = ex.message ?: "Internal Server Error",
                details = ex.stackTraceToString(),
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
