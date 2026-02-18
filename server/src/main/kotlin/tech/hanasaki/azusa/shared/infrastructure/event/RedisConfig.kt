package tech.hanasaki.azusa.shared.infrastructure.event

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String,
)

fun ApplicationConfig.readRedisConfig(): RedisConfig =
    RedisConfig(
        host = requireString("redis.host"),
        port = requireString("redis.port").toInt(),
        password = requireString("redis.password"),
    )
