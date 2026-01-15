package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.api.authRoutes
import tech.hanasaki.azusa.modules.auth.application.service.AuthService
import tech.hanasaki.azusa.modules.auth.application.service.OtpService

fun Application.configureRouting(): Unit {
    val authService: AuthService by inject()
    val otpService: OtpService by inject()

    routing {
        get("/health") {
            call.respondText("ok")
        }

        val openApiResource = environment.classLoader.getResource("openapi/documentation.yaml")
        if (openApiResource != null) {
            openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        }
        authRoutes(
            authService,
            otpService
        )
    }
}
