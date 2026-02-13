package tech.hanasaki.azusa.shared.infrastructure.config

import io.ktor.server.config.*

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

data class S3Config(
    val endpoint: String,
    val region: String,
    val avatarBucket: String,
    val knowledgeBucket: String,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
)

fun ApplicationConfig.readS3Config(): S3Config {
    return S3Config(
        endpoint = requireString("s3.endpoint"),
        region = requireString("s3.region"),
        avatarBucket = requireString("s3.avatarBucket"),
        knowledgeBucket = requireString("s3.knowledgeBucket"),
        accessKey = requireString("s3.accessKey"),
        secretKey = requireString("s3.secretKey"),
        publicBaseUrl = requireString("s3.publicBaseUrl"),
        forcePathStyle = requireString("s3.forcePathStyle").toBoolean(),
    )
}

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

fun ApplicationConfig.requireString(path: String): String {
    return property(path).getString()
}

fun ApplicationConfig.optionalString(path: String): String? =
    propertyOrNull(path)?.getString()

fun ApplicationConfig.optionalInt(path: String): Int? =
    optionalString(path)?.toIntOrNull()

fun ApplicationConfig.optionalLong(path: String): Long? =
    optionalString(path)?.toLongOrNull()

fun ApplicationConfig.optionalBoolean(path: String): Boolean? =
    optionalString(path)?.toBooleanStrictOrNull()
