package tech.hanasaki.azusa.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import tech.hanasaki.azusa.common.platform.util.AppJson


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(AppJson.json)
    }
}

