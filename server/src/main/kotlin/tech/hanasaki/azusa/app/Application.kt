package tech.hanasaki.azusa.app

import io.ktor.server.application.*
import io.ktor.server.netty.*
import tech.hanasaki.azusa.db.DatabaseFactory
import tech.hanasaki.azusa.routes.configureRouting

fun main(args: Array<String>): Unit {
    EngineMain.main(args)
}

fun Application.module(): Unit {
    DatabaseFactory.init(environment.config)
    configureSerialization()
    configureCors()
    configureStatusPages()
    configureSecurity(environment.config)
    configureRouting()
}
