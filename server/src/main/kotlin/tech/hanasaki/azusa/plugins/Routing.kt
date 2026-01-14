package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.api.authRoutes
import tech.hanasaki.azusa.modules.auth.application.service.AuthService

fun Application.configureRouting(): Unit {
    val authService: AuthService by inject()

    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        authRoutes(
            authService,
        )
    }
}
