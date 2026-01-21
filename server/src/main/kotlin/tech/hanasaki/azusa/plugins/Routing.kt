package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tech.hanasaki.azusa.modules.auth.api.authRoutes
import tech.hanasaki.azusa.modules.character.api.characterRoutes
import tech.hanasaki.azusa.modules.setting.api.settingRoutes
import tech.hanasaki.azusa.modules.theme.api.themeRoutes

fun Application.configureRouting(): Unit {

    routing {
        get("/health") {
            call.respondText("ok")
        }

        val openApiResource = environment.classLoader.getResource("openapi/documentation.yaml")
        if (openApiResource != null) {
            openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        }
        authRoutes()
        settingRoutes()
        themeRoutes()
        characterRoutes()
    }
}
