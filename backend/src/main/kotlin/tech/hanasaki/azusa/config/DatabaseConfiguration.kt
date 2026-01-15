package tech.hanasaki.azusa.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.hanasaki.azusa.shared.infrastructure.database.DatabaseFactory

@Configuration
class DatabaseConfiguration(
    private val databaseConfig: DatabaseConfig,
) {
    @Bean
    fun dataSource(): HikariDataSource = DatabaseFactory.init(databaseConfig)
}
