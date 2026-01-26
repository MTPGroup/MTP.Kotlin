package tech.hanasaki.azusa.common.infrastructure.external

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AppLogger {
    @Bean
    fun getLogger(): Logger =
        LoggerFactory.getLogger(javaClass)
}