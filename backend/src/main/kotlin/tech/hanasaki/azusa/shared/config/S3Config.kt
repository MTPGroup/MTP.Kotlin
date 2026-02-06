package tech.hanasaki.azusa.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "s3")
data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val knowledgeBucket: String = bucket,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
)