package tech.hanasaki.azusa.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(config: ApplicationConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            driverClassName = config.property("database.driver").getString()
            jdbcUrl = config.property("database.url").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = config.property("database.maxPoolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        val dataSource = HikariDataSource(hikariConfig)
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        Database.connect(dataSource)
        return dataSource
    }
}
