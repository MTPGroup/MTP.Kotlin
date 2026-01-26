package tech.hanasaki.azusa.modules.knowledge.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeBaseStats
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.modules.knowledge.domain.port.SearchResult
import tech.hanasaki.azusa.common.kernel.model.PageResult
import java.util.*

@Serializable
data class KnowledgeBaseResponse(
    @Contextual val id: UUID,
    val name: String,
    val description: String?,
    @Contextual val authorId: UUID,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PagedKnowledgeBaseResponse(
    val items: List<KnowledgeBaseResponse>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

@Serializable
data class KnowledgeFileResponse(
    @Contextual val id: UUID,
    @Contextual val knowledgeBaseId: UUID,
    val fileName: String,
    val fileSize: Long?,
    val fileType: String?,
    val status: FileStatus,
    val errorMessage: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class KnowledgeBaseStatsResponse(
    @Contextual val knowledgeBaseId: UUID,
    val fileCount: Int,
    val documentCount: Long,
)

@Serializable
data class SearchResultResponse(
    @Contextual val documentId: UUID,
    @Contextual val knowledgeBaseId: UUID,
    val content: String,
    val metadata: JsonObject,
    val similarity: Float,
)

fun KnowledgeBase.toResponse(): KnowledgeBaseResponse = KnowledgeBaseResponse(
    id = id.value,
    name = name,
    description = description,
    authorId = authorId.value,
    isPublic = isPublic,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun PageResult<KnowledgeBase>.toResponse(): PagedKnowledgeBaseResponse = PagedKnowledgeBaseResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)

fun KnowledgeFile.toResponse(): KnowledgeFileResponse = KnowledgeFileResponse(
    id = id.value,
    knowledgeBaseId = knowledgeBaseId.value,
    fileName = fileName,
    fileSize = fileSize,
    fileType = fileType,
    status = status,
    errorMessage = errorMessage,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun KnowledgeBaseStats.toResponse(): KnowledgeBaseStatsResponse = KnowledgeBaseStatsResponse(
    knowledgeBaseId = knowledgeBaseId.value,
    fileCount = fileCount,
    documentCount = documentCount,
)

fun SearchResult.toResponse(): SearchResultResponse = SearchResultResponse(
    documentId = documentId.value,
    knowledgeBaseId = knowledgeBaseId.value,
    content = content,
    metadata = metadata,
    similarity = similarity,
)
