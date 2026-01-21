package tech.hanasaki.azusa.shared.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import tech.hanasaki.azusa.shared.domain.model.UserId
import java.util.*

fun ApplicationCall.requireUserId(): UserId {
    val principal = principal<JWTPrincipal>() ?: throw ApiException(
        HttpStatusCode.Unauthorized,
        "UNAUTHORIZED",
        "Missing authentication"
    )
    return principal.subject?.let { runCatching { UserId(UUID.fromString(it)) }.getOrNull() }
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Invalid subject")
}

