package tech.hanasaki.momotalk_plus.core.network

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.util.network.*
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class NetworkErrorMapper(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {

    suspend fun map(throwable: Throwable): AppError {
        return when (throwable) {
            is ResponseException -> mapResponseException(throwable)
            is UnresolvedAddressException -> AppError.Network(causeMessage = throwable.message)
            is IOException -> AppError.Network(causeMessage = throwable.message)
            is SerializationException, is JsonConvertException -> {
                AppError.Serialization(causeMessage = throwable.message)
            }

            else -> AppError.Unknown(causeMessage = throwable.message)
        }
    }

    private suspend fun mapResponseException(exception: ResponseException): AppError {
        val status = exception.response.status
        val body = runCatching { exception.response.bodyAsText() }.getOrNull()
        val envelope = body?.let { runCatching { json.decodeFromString<ApiEnvelope<Nothing>>(it) }.getOrNull() }

        val message = envelope?.message ?: status.description
        val code = envelope?.code
        val details = envelope?.errors

        return when (status) {
            HttpStatusCode.BadRequest -> AppError.Validation(message = message, details = details, code = code)
            HttpStatusCode.Unauthorized -> AppError.Unauthorized(message = message, code = code)
            HttpStatusCode.Forbidden -> AppError.Forbidden(message = message, code = code)
            HttpStatusCode.NotFound -> AppError.NotFound(message = message, code = code)
            HttpStatusCode.Conflict -> AppError.Conflict(message = message, code = code)
            HttpStatusCode.TooManyRequests -> {
                val retryAfter = exception.response.headers["Retry-After"]?.toLongOrNull()
                AppError.RateLimited(message = message, retryAfter = retryAfter, code = code)
            }

            else -> AppError.Http(
                status = status.value,
                message = message,
                code = code,
                details = details,
            )
        }
    }
}
