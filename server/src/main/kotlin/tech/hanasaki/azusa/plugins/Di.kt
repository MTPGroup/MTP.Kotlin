package tech.hanasaki.azusa.plugins

import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.ktor.plugin.Koin
import tech.hanasaki.azusa.di.appModules

fun Application.configureDi(config: ApplicationConfig) {
    install(Koin) {
        modules(appModules(config))
    }
}
