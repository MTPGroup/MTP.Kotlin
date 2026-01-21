package tech.hanasaki.azusa.shared

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.config.readDatabaseConfig
import tech.hanasaki.azusa.shared.infrastructure.database.DatabaseFactory

fun databaseModule(config: ApplicationConfig) = module {
    single(createdAtStart = true) { DatabaseFactory.init(config.readDatabaseConfig()) }
}
