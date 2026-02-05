package tech.hanasaki.azusa

import io.ktor.server.config.*
import org.koin.core.module.Module
import tech.hanasaki.azusa.common.adapter.out.event.eventModule
import tech.hanasaki.azusa.common.adapter.out.persistence.databaseModule
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.chat.chatModule
import tech.hanasaki.azusa.modules.knowledge.knowledgeModule
import tech.hanasaki.azusa.modules.plugin.pluginModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        databaseModule(config),
        eventModule(config),
//        notificationModule(config),
        authModule(config),
        settingModule(config),
        themeModule(config),
        characterModule(config),
        chatModule(config),
        pluginModule(config),
        knowledgeModule(config)
    )
}

