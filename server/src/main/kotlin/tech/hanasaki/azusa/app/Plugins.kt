package tech.hanasaki.azusa.app

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.ApiException
import java.util.UUID

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
    val timestamp: String,
)

@Serializable
data class ErrorDetail(
    val message: String,
    val code: String = "INTERNAL_SERVER_ERROR",
    val details: String? = null,
)

fun Application.configureSerialization(): Unit {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureCors(): Unit {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost()
    }
}

fun Application.configureStatusPages(): Unit {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            val payload = ErrorResponse(
                error = ErrorDetail(
                    message = cause.message ?: "Request Failed",
                    code = cause.code,
                    details = cause.detail,
                ),
                timestamp = Clock.System.now().toString(),
            )
            call.respond(cause.status, payload)
        }
        exception<Throwable> { call, cause ->
            val payload = ErrorResponse(
                error = ErrorDetail(
                    message = cause.message ?: "Internal Server Error",
                    details = cause.stackTraceToString(),
                ),
                timestamp = Clock.System.now().toString(),
            )
            call.respond(HttpStatusCode.InternalServerError, payload)
        }
    }
}

fun Application.configureSecurity(config: ApplicationConfig): Unit {
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val realm = config.property("jwt.realm").getString()
    val secret = config.property("jwt.secret").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                val subject = credential.subject ?: return@validate null
                runCatching { UUID.fromString(subject) }.getOrNull() ?: return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun Route.healthRoutes(): Unit {
    route("/health") {
        get {
            call.respondText("ok")
        }
    }
}
