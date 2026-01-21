package tech.hanasaki.azusa

import io.ktor.server.application.*
import io.ktor.server.netty.*
import tech.hanasaki.azusa.plugins.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDi(environment.config)
    configureSerialization()
    configureCors()
    configureApiResponseWrapper()
    configureStatusPages()
    configureSecurity()
    configureRouting()
}
