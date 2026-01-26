package tech.hanasaki.azusa.common.platform.di

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.common.platform.database.DatabaseFactory
import tech.hanasaki.azusa.readDatabaseConfig

fun databaseModule(config: ApplicationConfig) = module {
    single(createdAtStart = true) { DatabaseFactory.init(config.readDatabaseConfig()) }
}
