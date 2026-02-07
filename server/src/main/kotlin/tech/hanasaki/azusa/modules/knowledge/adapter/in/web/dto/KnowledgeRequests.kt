package tech.hanasaki.azusa.modules.knowledge.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateKnowledgeBaseRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
)

@Serializable
data class UpdateKnowledgeBaseRequest(
    val name: String,
    val description: String?,
    val isPublic: Boolean,
)

@Serializable
data class UploadFileRequest(
    val fileName: String,
    val filePath: String,
    val fileSize: Long? = null,
    val fileType: String? = null,
)


@Serializable
data class SearchKnowledgeRequest(
    val query: String,
    val knowledgeBaseIds: List<Uuid>? = null,
    val threshold: Float = 0.7f,
    val limit: Int = 10,
)
