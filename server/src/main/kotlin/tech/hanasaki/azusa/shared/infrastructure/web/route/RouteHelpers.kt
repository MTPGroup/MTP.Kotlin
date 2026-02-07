package tech.hanasaki.azusa.shared.infrastructure.web.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import kotlin.time.Clock
import kotlin.uuid.Uuid


fun ApplicationCall.requireUserId(): UserId {
    val principal = principal<JWTPrincipal>() ?: throw AuthorizationException()
    return principal.subject?.let { runCatching { UserId(Uuid.parse(it)) }.getOrNull() }
        ?: throw AuthorizationException()
}

fun ApplicationCall.requireAdmin() {
    val principal = principal<JWTPrincipal>() ?: throw AuthorizationException()
    val role = principal.payload.getClaim("role")?.asString() ?: "USER"
    if (role != "ADMIN") {
        throw AuthorizationException("需要管理员权限")
    }
}

fun ApplicationCall.uuidParam(name: String): Uuid {
    val value = parameters[name] ?: throw ValidationException()
    return runCatching { Uuid.parse(value) }.getOrElse {
        throw ValidationException("$name 必须是 UUID 格式")
    }
}

suspend fun ApplicationCall.respondNotImplemented(message: String) {
    respond(
        HttpStatusCode.NotImplemented,
        ApiResponse(
            message = message,
            data = null,
            timestamp = Clock.System.now(),
            success = false,
        ),
    )
}

