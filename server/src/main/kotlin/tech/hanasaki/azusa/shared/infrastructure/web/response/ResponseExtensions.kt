package tech.hanasaki.azusa.shared.infrastructure.web.response

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend inline fun <reified T> RoutingCall.respondOk(
    data: T,
    message: String = "操作成功",
    statusCode: HttpStatusCode = HttpStatusCode.OK,
) {
    respond(statusCode, ApiResponse.ok(data, message))
}

suspend inline fun <reified T> RoutingCall.respondError(
    message: String,
    code: String = "ERROR",
    errors: Map<String, String>? = null,
    statusCode: HttpStatusCode = HttpStatusCode.BadRequest,
) {
    respond(statusCode, ApiResponse.error<T>(message, code, errors))
}

