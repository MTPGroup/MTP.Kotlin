package tech.hanasaki.azusa.bootstrap

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.authRoutes
import tech.hanasaki.azusa.modules.character.api.characterRoutes
import tech.hanasaki.azusa.modules.knowledge.api.knowledgeRoutes
import tech.hanasaki.azusa.modules.plugin.api.pluginRoutes
import tech.hanasaki.azusa.modules.setting.api.settingRoutes
import tech.hanasaki.azusa.modules.theme.api.themeRoutes

fun Application.configureRouting() {
    routing {
        swaggerUI("/swaggerUI") {
            info = OpenApiInfo("Azusa", "1.0")
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }

        authRoutes()
        settingRoutes()
        themeRoutes()
        characterRoutes()
        pluginRoutes()
        knowledgeRoutes()
    }
}
