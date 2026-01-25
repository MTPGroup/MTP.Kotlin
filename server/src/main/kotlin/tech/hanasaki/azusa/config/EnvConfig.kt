package tech.hanasaki.azusa.config

import io.ktor.server.config.*

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)


data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val knowledgeBucket: String,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
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


fun ApplicationConfig.readS3Config(): S3Config {
    val bucket = requireString("s3.bucket")
    return S3Config(
        endpoint = requireString("s3.endpoint"),
        region = requireString("s3.region"),
        bucket = bucket,
        knowledgeBucket = propertyOrNull("s3.knowledgeBucket")?.getString() ?: bucket,
        accessKey = requireString("s3.accessKey"),
        secretKey = requireString("s3.secretKey"),
        publicBaseUrl = requireString("s3.publicBaseUrl"),
        forcePathStyle = requireString("s3.forcePathStyle").toBoolean(),
    )
}

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
