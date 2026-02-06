package tech.hanasaki.azusa.modules.auth.adapter.`in`.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.config.JwtConfig
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import kotlin.time.Clock
import kotlin.uuid.Uuid


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
                    "过期或非法的令牌"
                } else {
                    "没有权限"
                }
                val payload = ApiResponse<Nothing>(
                    success = false,
                    message = message,
                    errors = mapOf(
                        "code" to "UNAUTHORIZED",
                        "message" to message,
                    ),
                    timestamp = Clock.System.now(),
                )
                call.respond(HttpStatusCode.Unauthorized, payload)
            }
            validate { credential ->
                val subject = credential.subject ?: return@validate null
                runCatching { Uuid.parse(subject) }.getOrNull() ?: return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}
