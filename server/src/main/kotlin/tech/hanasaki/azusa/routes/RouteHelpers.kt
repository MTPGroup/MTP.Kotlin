package tech.hanasaki.azusa.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.datetime.Clock
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.common.ApiResponse
import java.util.UUID

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
