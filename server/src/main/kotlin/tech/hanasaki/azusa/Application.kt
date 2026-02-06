package tech.hanasaki.azusa

import io.ktor.server.application.*
import io.ktor.server.netty.*
import tech.hanasaki.azusa.bootstrap.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.configureSecurity
import tech.hanasaki.azusa.shared.infrastructure.event.configureEvents
import tech.hanasaki.azusa.shared.infrastructure.web.error.configureErrorHandling

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDi(environment.config)
    configureSerialization()
    configureCors()
    configureErrorHandling()
    configureSecurity()
    configureEvents()
    configureRouting()
}
