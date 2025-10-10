package tech.hanasaki.momotalk_plus.core.data.repository

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.api.UploadApi
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository

class UploadImageRepositoryImpl(
    private val uploadApi: UploadApi,
) : UploadImageRepository {
    override suspend fun uploadImage(
        imageData: ImageData,
        path: UploadPath,
        userId: String?,
    ): IResult<String, AppError> {
        val pathString = when (path) {
            UploadPath.USER_AVATAR -> "user-avatar"
            UploadPath.USER_BACKGROUND -> "user-background"
            UploadPath.CHARACTER_AVATAR -> "character-avatar"
            UploadPath.CHARACTER_BACKGROUND -> "character-background"
            UploadPath.CHAT_IMAGE -> "chat-image"
            UploadPath.GENERAL -> "general"
        }

        val parts = mutableListOf<PartData>()

        parts.add(PartData.FormItem(pathString, { }, Headers.build {
            append(HttpHeaders.ContentDisposition, "form-data; name=\"path\"")
        }))

        println("userId: $userId")
        userId?.let {
            parts.add(PartData.FormItem(it, { }, Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"userId\"")
            }))
        }

        parts.add(
            PartData.BinaryItem(
                provider = { buildPacket { writeFully(imageData.byteArray) } },
                dispose = { },
                partHeaders = Headers.build {
                    append(HttpHeaders.ContentType, imageData.mimeType)
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"image\"; filename=\"${imageData.fileName}\""
                    )
                }
            ))

        return try {
            val response = uploadApi.uploadImage(
                MultiPartFormDataContent(parts)
            )
            IResult.Success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }
}