package tech.hanasaki.azusa

import io.ktor.server.config.*
import org.koin.core.module.Module
import tech.hanasaki.azusa.modules.auth.authModule
import tech.hanasaki.azusa.modules.character.characterModule
import tech.hanasaki.azusa.modules.chat.chatModule
import tech.hanasaki.azusa.modules.knowledge.knowledgeModule
import tech.hanasaki.azusa.modules.notification.notificationModule
import tech.hanasaki.azusa.modules.plugin.pluginModule
import tech.hanasaki.azusa.modules.setting.settingModule
import tech.hanasaki.azusa.modules.theme.themeModule
import tech.hanasaki.azusa.shared.infrastructure.event.eventModule
import tech.hanasaki.azusa.shared.infrastructure.persistence.databaseModule
import tech.hanasaki.azusa.shared.infrastructure.security.securityModule

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        databaseModule(config),
        securityModule(),
        eventModule(config),
        notificationModule(config),
        authModule(config),
        settingModule(config),
        themeModule(config),
        characterModule(config),
        chatModule(config),
        pluginModule(config),
        knowledgeModule(config)
    )
}