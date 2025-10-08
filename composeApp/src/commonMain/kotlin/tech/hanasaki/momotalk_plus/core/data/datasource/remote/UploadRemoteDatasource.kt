package tech.hanasaki.momotalk_plus.core.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import tech.hanasaki.momotalk_plus.core.data.model.UploadResponse
import tech.hanasaki.momotalk_plus.core.domain.model.AppError
import tech.hanasaki.momotalk_plus.core.domain.model.IResult
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.domain.model.UploadPath

class UploadRemoteDatasource(client: HttpClient) : BaseRemoteDatasource(client) {

    private val endpoint = "$baseUrl/uploads/images"

    suspend fun upload(
        imageData: ImageData,
        path: UploadPath,
        userId: String? = null,
    ): IResult<String, AppError> {
        val pathString = when (path) {
            UploadPath.USER_AVATAR -> "user-avatar"
            UploadPath.USER_BACKGROUND -> "user-background"
            UploadPath.CHARACTER_AVATAR -> "character-avatar"
            UploadPath.CHARACTER_BACKGROUND -> "character-background"
            UploadPath.CHAT_IMAGE -> "chat-image"
            UploadPath.GENERAL -> "general"
        }

        return try {
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

            val httpResponse = client.post(endpoint) {
                setBody(MultiPartFormDataContent(parts))
            }

            if (httpResponse.status == HttpStatusCode.OK) {
                val response = httpResponse.body<UploadResponse>()
                IResult.Success(response.data.url)
            } else {
                val errorBody = httpResponse.bodyAsText()
                IResult.Error(AppError("上传失败: $errorBody"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            IResult.Error(AppError(e.message ?: "未知错误"))
        }
    }
}