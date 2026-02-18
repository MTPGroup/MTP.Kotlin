package tech.hanasaki.azusa.shared.infrastructure.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import java.net.URI

class S3Storage(private val config: S3Config) : FileStoragePort {
    private val logger = KotlinLogging.logger { }

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

    override fun upload(objectKey: String, contentType: String, bytes: ByteArray): String {
        putObject(config.bucket, objectKey, contentType, bytes)
        return objectKey
    }

    override fun delete(objectKey: String) {
        val request = DeleteObjectRequest.builder()
            .bucket(config.bucket)
            .key(objectKey)
            .build()
        client.deleteObject(request)
    }

    override fun deleteDirectory(type: String, prefix: String) {
        val pre = when (type) {
            "avatar" -> "avatars/$prefix"
            "knowledge-base" -> "knowledge/$prefix"
            else -> prefix
        }
        logger.debug { "开始删除: $pre 下的文件" }
        val bucket = config.bucket

        var continuationToken: String? = null

        while (true) {
            val listResponse = client.listObjectsV2 {
                it.bucket(bucket)
                    .prefix(pre)
                    .continuationToken(continuationToken)
            }

            val objects = listResponse.contents()
            if (objects.isNotEmpty()) {
                client.deleteObjects { del ->
                    del.bucket(bucket)
                        .delete { d ->
                            d.objects(
                                objects.map {
                                    ObjectIdentifier.builder()
                                        .key(it.key())
                                        .build()
                                }
                            )
                        }
                }
            }

            if (!listResponse.isTruncated) break
            continuationToken = listResponse.nextContinuationToken()
        }
    }

    override fun download(objectKey: String): ByteArray {
        val request = GetObjectRequest.builder()
            .bucket(config.bucket)
            .key(objectKey)
            .build()
        return client.getObject(request).readAllBytes()
    }

    override fun publicUrl(objectKey: String): String {
        val base = config.publicBaseUrl.trimEnd('/')
        return "$base/${config.bucket}/$objectKey"
    }

    private fun putObject(bucket: String, key: String, contentType: String, bytes: ByteArray) {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()
        client.putObject(request, RequestBody.fromBytes(bytes))
    }
}
