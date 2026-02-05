package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.authRoutes
import tech.hanasaki.azusa.modules.character.api.characterRoutes
import tech.hanasaki.azusa.modules.knowledge.api.knowledgeRoutes
import tech.hanasaki.azusa.modules.plugin.api.pluginRoutes
import tech.hanasaki.azusa.modules.setting.api.settingRoutes
import tech.hanasaki.azusa.modules.theme.api.themeRoutes

fun Application.configureRouting() {

    routing {
        authRoutes()
        settingRoutes()
        themeRoutes()
        characterRoutes()
        pluginRoutes()
        knowledgeRoutes()
    }
}
