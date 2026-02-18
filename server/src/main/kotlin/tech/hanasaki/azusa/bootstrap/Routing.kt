package tech.hanasaki.azusa.bootstrap

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import org.koin.ktor.ext.get
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.authRoutes
import tech.hanasaki.azusa.modules.character.adapter.`in`.web.characterRoutes
import tech.hanasaki.azusa.modules.chat.adapter.`in`.web.chatRoutes
import tech.hanasaki.azusa.modules.knowledge.adapter.`in`.web.knowledgeRoutes
import tech.hanasaki.azusa.modules.knowledge.application.service.PendingFileProcessor
import tech.hanasaki.azusa.modules.plugin.adapter.`in`.web.pluginRoutes
import tech.hanasaki.azusa.modules.setting.adapter.`in`.web.settingRoutes
import tech.hanasaki.azusa.modules.theme.api.themeRoutes

fun Application.configureRouting() {
    val pendingFileProcessor = get<PendingFileProcessor>()
    pendingFileProcessor.start()

    routing {
        swaggerUI("/swaggerUI") {
            info = OpenApiInfo("Azusa", "1.0")
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }

        route("/v1") {
            authRoutes()
            settingRoutes()
            themeRoutes()
            characterRoutes()
            pluginRoutes()
            knowledgeRoutes()
            chatRoutes()
        }
    }
}
