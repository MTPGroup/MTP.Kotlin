package tech.hanasaki.azusa.common.infrastructure.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.common.ApiResponse
import tech.hanasaki.azusa.common.AuthenticationException
import tech.hanasaki.azusa.common.AuthorizationException
import tech.hanasaki.azusa.common.AzusaException
import tech.hanasaki.azusa.common.ConflictException
import tech.hanasaki.azusa.common.ErrorDetail
import tech.hanasaki.azusa.common.NotFoundException
import kotlin.time.Clock

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ApiResponse<Nothing>> {
        val payload = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Request Failed",
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
    fun handleDomainException(ex: AzusaException): ResponseEntity<ApiResponse<Nothing>> {
        val (status, code) = when (ex) {
            is AuthenticationException -> HttpStatus.UNAUTHORIZED to "AUTHENTICATION_ERROR"
            is AuthorizationException -> HttpStatus.FORBIDDEN to "AUTHORIZATION_ERROR"
            is NotFoundException -> HttpStatus.NOT_FOUND to "NOT_FOUND"
            is ConflictException -> HttpStatus.CONFLICT to "CONFLICT"
            else -> HttpStatus.BAD_REQUEST to "DOMAIN_ERROR"
        }
        val payload = ApiResponse<Nothing>(
            success = false,
            message = ex.message,
            error = ErrorDetail(
                message = ex.message,
                code = code,
                details = ex.stackTraceToString(),
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val payload = ApiResponse<Nothing>(
            success = false,
            message = "Invalid request body",
            error = ErrorDetail(
                message = "Invalid request body",
                code = "BAD_REQUEST",
                details = ex.cause?.message ?: ex.message,
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(ex: Throwable): ResponseEntity<ApiResponse<Nothing>> {
        val payload = ApiResponse<Nothing>(
            success = false,
            message = ex.message ?: "Internal Server Error",
            error = ErrorDetail(
                message = ex.message ?: "Internal Server Error",
                details = ex.stackTraceToString(),
            ),
            timestamp = Clock.System.now().toString(),
        )
        return ResponseEntity(payload, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
