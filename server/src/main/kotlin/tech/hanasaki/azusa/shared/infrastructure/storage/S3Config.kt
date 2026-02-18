package tech.hanasaki.azusa.shared.infrastructure.storage

import io.ktor.server.config.*
import tech.hanasaki.azusa.shared.infrastructure.config.requireString

data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
)

fun ApplicationConfig.readS3Config(): S3Config {
    return S3Config(
        endpoint = requireString("s3.endpoint"),
        region = requireString("s3.region"),
        bucket = requireString("s3.bucket"),
        accessKey = requireString("s3.accessKey"),
        secretKey = requireString("s3.secretKey"),
        publicBaseUrl = requireString("s3.publicBaseUrl"),
        forcePathStyle = requireString("s3.forcePathStyle").toBoolean(),
    )
}
