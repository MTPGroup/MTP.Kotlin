package tech.hanasaki.azusa.common.platform.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import tech.hanasaki.azusa.S3Config
import java.net.URI

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
        putObject(config.bucket, objectKey, contentType, bytes)
        return buildPublicUrl(objectKey)
    }

    fun uploadKnowledgeFile(objectKey: String, contentType: String, bytes: ByteArray): String {
        putObject(config.knowledgeBucket, objectKey, contentType, bytes)
        return objectKey
    }

    fun deleteKnowledgeFile(objectKey: String): Unit {
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
        return "$base/${config.bucket}/$objectKey"
    }
}