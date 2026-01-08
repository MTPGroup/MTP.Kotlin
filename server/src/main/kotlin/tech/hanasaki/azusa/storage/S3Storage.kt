package tech.hanasaki.azusa.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.core.sync.RequestBody
import java.net.URI

data class S3Config(
    val endpoint: String,
    val region: String,
    val bucket: String,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val forcePathStyle: Boolean,
)

class S3Storage(private val config: S3Config) {
    private val client: S3Client = S3Client.builder()
        .endpointOverride(URI.create(config.endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.accessKey, config.secretKey)
            )
        )
        .region(Region.of(config.region))
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(config.forcePathStyle)
                .build()
        )
        .build()

    fun uploadAvatar(objectKey: String, contentType: String, bytes: ByteArray): String {
        val request = PutObjectRequest.builder()
            .bucket(config.bucket)
            .key(objectKey)
            .contentType(contentType)
            .build()
        client.putObject(request, RequestBody.fromBytes(bytes))
        return buildPublicUrl(objectKey)
    }

    private fun buildPublicUrl(objectKey: String): String {
        val base = config.publicBaseUrl.trimEnd('/')
        return "$base/${config.bucket}/$objectKey"
    }
}
