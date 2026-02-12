package tech.hanasaki.azusa.bootstrap

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import tech.hanasaki.azusa.shared.infrastructure.llm.llmModule
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
import tech.hanasaki.azusa.shared.infrastructure.storage.storageModule

fun Application.configureDi(config: ApplicationConfig) {
    install(Koin) {
        modules(appModules(config))
    }
}

fun appModules(config: ApplicationConfig): List<Module> {
    return listOf(
        databaseModule(config),
        storageModule(config),
        securityModule(),
        eventModule(config),
        notificationModule(config),
        authModule(config),
        settingModule(),
        themeModule(config),
        characterModule(),
        pluginModule(),
        llmModule(),
        chatModule(config),
        knowledgeModule()
    )
}
