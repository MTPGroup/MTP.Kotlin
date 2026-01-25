package tech.hanasaki.azusa.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.JwtConfig
import tech.hanasaki.azusa.shared.api.response.ApiResponse
import tech.hanasaki.azusa.shared.api.response.ErrorDetail
import java.util.*
import kotlin.time.Clock

fun Application.configureSecurity() {
    val jwtConfig: JwtConfig by inject()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            challenge { _, _ ->
                val header = call.request.headers[HttpHeaders.Authorization].orEmpty()
                val message = if (header.startsWith("Bearer ")) {
                    "Invalid or expired token"
                } else {
                    "Missing authentication"
                }
                val payload = ApiResponse<Nothing>(
                    success = false,
                    message = message,
                    error = ErrorDetail(
                        message = message,
                        code = "UNAUTHORIZED",
                    ),
                    timestamp = Clock.System.now(),
                )
                call.respond(HttpStatusCode.Unauthorized, payload)
            }
            validate { credential ->
                val subject = credential.subject ?: return@validate null
                runCatching { UUID.fromString(subject) }.getOrNull() ?: return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}
