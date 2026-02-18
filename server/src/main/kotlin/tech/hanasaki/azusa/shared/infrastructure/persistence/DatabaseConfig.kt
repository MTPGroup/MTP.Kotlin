package tech.hanasaki.azusa.shared.infrastructure.persistence

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)

fun ApplicationConfig.readDatabaseConfig(): DatabaseConfig {
    return DatabaseConfig(
        driver = requireString("database.driver"),
        url = requireString("database.url"),
        user = requireString("database.user"),
        password = requireString("database.password"),
        maxPoolSize = requireString("database.maxPoolSize").toInt(),
    )
}
