package tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope

class ProfileRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun updateMe(request: UpdateProfileRequest): ApiEnvelope<SuccessResponse> =
        client.put("auth/me") {
            setBody(request)
        }.body()

    suspend fun uploadAvatar(avatar: ImageData): ApiEnvelope<UploadAvatarResponse> =
        client.post("auth/me/avatar") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file", avatar.byteArray,
                            Headers.build {
                                append(HttpHeaders.ContentType, avatar.mimeType)
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"file\"; filename=\"${avatar.fileName}\"",
                                )
                            }
                        )
                    }
                )
            )
        }.body()
}