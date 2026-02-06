package tech.hanasaki.azusa.shared.infrastructure.persistence

import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.dsl.onClose
import tech.hanasaki.azusa.shared.infrastructure.config.readDatabaseConfig
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

fun databaseModule(config: ApplicationConfig) = module {
    single(createdAtStart = true) { DatabaseFactory.init(config.readDatabaseConfig()) }.onClose {
        it?.close()
    }
    single<TransactionalPort> { ExposedTransactionAdapter() }
}
