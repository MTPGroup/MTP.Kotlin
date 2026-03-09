package tech.hanasaki.momotalk_plus.core.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UploadCharacterAvatarResponse

class UploadImageRepositoryImpl(
    private val client: HttpClient,
) : UploadImageRepository {
    override suspend fun uploadImage(
        imageData: ImageData,
        path: UploadPath,
        userId: String?,
    ): String {
        return when (path) {
            UploadPath.AVATAR -> {
                if (!userId.isNullOrBlank()) {
                    client.post("characters/$userId/avatar") {
                        setBody(imageData.toMultipartContent())
                    }.body<ApiEnvelope<UploadCharacterAvatarResponse>>().data?.avatar
                } else {
                    client.post("auth/me/avatar") {
                        setBody(imageData.toMultipartContent())
                    }.body<ApiEnvelope<UploadAvatarResponse>>().data?.avatar
                } ?: throw IllegalStateException("上传成功但未返回头像地址")
            }

            UploadPath.BACKGROUND, UploadPath.GENERAL -> {
                throw UnsupportedOperationException("后端暂未提供通用图片上传接口")
            }
        }
    }
}

private fun ImageData.toMultipartContent(): MultiPartFormDataContent =
    MultiPartFormDataContent(
        formData {
            append(
                "file",
                byteArray,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"file\"; filename=\"$fileName\"",
                    )
                }
            )
        }
    )

@kotlinx.serialization.Serializable
private data class UploadAvatarResponse(
    val avatar: String,
)
