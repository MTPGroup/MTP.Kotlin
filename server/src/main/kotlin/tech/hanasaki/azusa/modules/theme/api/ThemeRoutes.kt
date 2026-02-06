package tech.hanasaki.azusa.modules.theme.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.theme.api.dto.CreateThemeRequest
import tech.hanasaki.azusa.modules.theme.api.dto.UpdateThemeRequest
import tech.hanasaki.azusa.modules.theme.api.dto.toResponse
import tech.hanasaki.azusa.modules.theme.application.service.ThemeService
import tech.hanasaki.azusa.shared.domain.model.vo.ThemeId
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam


fun Route.themeRoutes() {
    val themeService: ThemeService by inject()
    authenticate("auth-jwt") {
        route("/themes") {
            get {
                val userId = call.requireUserId()
                val themes = themeService.listThemes(userId).map { it.toResponse() }
                call.respond(themes)
            }

            get("/{themeId}") {
                val userId = call.requireUserId()
                val themeId = ThemeId(call.uuidParam("themeId"))
                val theme = themeService.getTheme(userId, themeId)
                call.respond(theme.toResponse())
            }

            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateThemeRequest>()
                val theme = themeService.createTheme(userId, request.toCommand())
                call.respond(theme.toResponse())
            }

            put("/{themeId}") {
                val userId = call.requireUserId()
                val themeId = ThemeId(call.uuidParam("themeId"))
                val request = call.receive<UpdateThemeRequest>()
                val theme = themeService.updateTheme(userId, themeId, request.toCommand())
                call.respond(theme.toResponse())
            }

            delete("/{themeId}") {
                val userId = call.requireUserId()
                val themeId = ThemeId(call.uuidParam("themeId"))
                themeService.deleteTheme(userId, themeId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

