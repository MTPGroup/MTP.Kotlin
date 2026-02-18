package tech.hanasaki.azusa

import io.ktor.server.application.*
import io.ktor.server.netty.*
import tech.hanasaki.azusa.bootstrap.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.configureSecurity
import tech.hanasaki.azusa.shared.infrastructure.event.configureEvents
import tech.hanasaki.azusa.shared.infrastructure.web.error.configureErrorHandling

fun main(args: Array<String>) {
    println(
        """
        |
        |    $CYAN    ___                            $RST
        |    $CYAN   /   | ____  __  __________ _    $RST
        |    $CYAN  / /| |/_  / / / / / ___/ __` /   $RST
        |    $CYAN / ___ | / /_/ /_/ (__  ) /_/ /    $RST
        |    $CYAN/_/  |_|/___/\__,_/____/\__,_/     $RST
        |
        |    ${MAGENTA}Powered by Ktor · Kotlin · Koin$RST
        |
        """.trimMargin()
    )
    EngineMain.main(args)
}

private const val CYAN = "\u001B[36m"
private const val MAGENTA = "\u001B[35m"
private const val RST = "\u001B[0m"

fun Application.module() {
    configureDi(environment.config)
    configureSerialization()
    configureCors()
    configureErrorHandling()
    configureSecurity()
    configureEvents()
    configureRouting()
}
