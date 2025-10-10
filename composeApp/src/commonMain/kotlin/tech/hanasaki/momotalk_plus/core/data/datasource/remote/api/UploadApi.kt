package tech.hanasaki.momotalk_plus.core.data.datasource.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.client.request.forms.*
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto.UploadResponse

/**
 * Upload API - 使用 Ktorfit 定义的类型安全接口
 */
interface UploadApi {
    /**
     * 上传图片
     */
    @POST("uploads/images")
    suspend fun uploadImage(
        @Body parts: MultiPartFormDataContent,
    ): UploadResponse
}