package tech.hanasaki.momotalk_plus.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponseData(
    val url: String,
    val key: String,
    val originalName: String,
)

@Serializable
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val data: UploadResponseData,
)