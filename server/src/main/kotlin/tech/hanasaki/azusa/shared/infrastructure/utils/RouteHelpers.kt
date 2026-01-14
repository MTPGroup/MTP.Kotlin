package tech.hanasaki.azusa.shared.infrastructure.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import java.util.*

fun ApplicationCall.uuidParam(name: String): UUID {
    val value = parameters[name] ?: throw ApiException(
        HttpStatusCode.BadRequest,
        "VALIDATION_ERROR",
        "Missing parameter: $name",
    )
    return runCatching { UUID.fromString(value) }.getOrElse {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid UUID: $name")
    }
}

suspend fun ApplicationCall.respondNotImplemented(message: String): Unit {
    respond(
        HttpStatusCode.NotImplemented,
        ApiResponse(
            message = message,
            data = null,
            timestamp = Clock.System.now().toString(),
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
    if (limit < 1 || limit > 100) {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid limit")
    }
    return limit
}

fun parseUuid(value: String, name: String): UUID {
    return runCatching { UUID.fromString(value) }.getOrElse {
        throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid UUID: $name")
    }
}
