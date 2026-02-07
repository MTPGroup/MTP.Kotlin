package tech.hanasaki.azusa.shared.infrastructure.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import tech.hanasaki.azusa.shared.infrastructure.config.S3Config
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import java.net.URI

class S3Storage(private val config: S3Config) : FileStoragePort {
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

    override fun uploadAvatar(objectKey: String, contentType: String, bytes: ByteArray): String {
        putObject(config.avatarBucket, objectKey, contentType, bytes)
        return buildPublicUrl(objectKey)
    }

    override fun uploadFile(objectKey: String, contentType: String, bytes: ByteArray): String {
        putObject(config.knowledgeBucket, objectKey, contentType, bytes)
        return objectKey
    }

    override fun deleteFile(objectKey: String) {
        val request = DeleteObjectRequest.builder()
            .bucket(config.knowledgeBucket)
            .key(objectKey)
            .build()
        client.deleteObject(request)
    }

    private fun putObject(bucket: String, key: String, contentType: String, bytes: ByteArray) {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()
        client.putObject(request, RequestBody.fromBytes(bytes))
    }

    private fun buildPublicUrl(objectKey: String): String {
        val base = config.publicBaseUrl.trimEnd('/')
        return "$base/${config.avatarBucket}/$objectKey"
    }
}
