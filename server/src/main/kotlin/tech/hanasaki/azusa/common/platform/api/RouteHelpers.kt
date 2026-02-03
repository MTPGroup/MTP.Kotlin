package tech.hanasaki.azusa.common.platform.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Clock
import kotlin.uuid.Uuid


fun ApplicationCall.requireUserId(): UserId {
    val principal = principal<JWTPrincipal>() ?: throw ApiException(
        HttpStatusCode.Unauthorized,
        "UNAUTHORIZED",
        "Missing authentication"
    )
    return principal.subject?.let { runCatching { UserId(Uuid.parse(it)) }.getOrNull() }
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Invalid subject")
}


fun ApplicationCall.uuidParam(name: String): Uuid {
    val value = parameters[name] ?: throw ApiException(
        HttpStatusCode.BadRequest,
        "VALIDATION_ERROR",
        "Missing parameter: $name",
    )
    return runCatching { Uuid.parse(value) }.getOrElse {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid UUID: $name")
    }
}

suspend fun ApplicationCall.respondNotImplemented(message: String): Unit {
    respond(
        HttpStatusCode.NotImplemented,
        ApiResponse(
            message = message,
            data = null,
            timestamp = Clock.System.now(),
        ),
    )
}

fun parsePageParam(value: String?): Int {
    val page = value?.toIntOrNull() ?: 1
    if (page < 1) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid page")
    }
    return page
}

fun parseLimitParam(value: String?): Int {
    val limit = value?.toIntOrNull() ?: 20
    if (limit !in 1..100) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid limit")
    }
    return limit
}


fun parseUuid(value: String, name: String): Uuid {
    return runCatching { Uuid.parse(value) }.getOrElse {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid UUID: $name")
    }
}
