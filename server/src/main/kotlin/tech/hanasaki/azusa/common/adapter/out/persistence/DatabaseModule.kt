package tech.hanasaki.azusa.common.adapter.out.persistence

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.common.adapter.out.persistence.utils.DatabaseFactory
import tech.hanasaki.azusa.common.port.out.TransactionalPort
import tech.hanasaki.azusa.readDatabaseConfig

fun databaseModule(config: ApplicationConfig) = module {
    single(createdAtStart = true) { DatabaseFactory.init(config.readDatabaseConfig()) }
    single<TransactionalPort> { ExposedTransactionAdapter() }
}
