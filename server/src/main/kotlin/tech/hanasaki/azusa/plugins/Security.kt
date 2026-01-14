package tech.hanasaki.azusa.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.util.*

fun Application.configureSecurity(config: ApplicationConfig) {
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
