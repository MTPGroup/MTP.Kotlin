package tech.hanasaki.azusa.config

import io.ktor.server.config.*

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
)


data class OtpDebugConfig(
    val returnCode: Boolean,
)

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val from: String,
    val tls: Boolean,
    val enabled: Boolean,
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

fun ApplicationConfig.readJwtConfig(): JwtConfig {
    return JwtConfig(
        issuer = requireString("jwt.issuer"),
        audience = requireString("jwt.audience"),
        secret = requireString("jwt.secret"),
        accessTokenMinutes = requireString("jwt.accessTokenMinutes").toInt(),
        refreshTokenDays = requireString("jwt.refreshTokenDays").toInt(),
    )
}


fun ApplicationConfig.readOtpDebugConfig(): OtpDebugConfig {
    return OtpDebugConfig(
        returnCode = requireString("otp.debugReturn").toBoolean(),
    )
}

fun ApplicationConfig.readSmtpConfig(): SmtpConfig {
    return SmtpConfig(
        host = requireString("smtp.host"),
        port = requireString("smtp.port").toInt(),
        username = requireString("smtp.username"),
        password = requireString("smtp.password"),
        from = requireString("smtp.from"),
        tls = requireString("smtp.tls").toBoolean(),
        enabled = requireString("smtp.enabled").toBoolean(),
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

private fun ApplicationConfig.requireString(path: String): String {
    return property(path).getString()
}
