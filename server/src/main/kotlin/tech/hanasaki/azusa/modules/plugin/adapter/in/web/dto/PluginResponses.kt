package tech.hanasaki.azusa.modules.plugin.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.plugin.domain.model.HttpExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.KnowledgeSearchExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginExecutionConfig
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import kotlin.uuid.Uuid


@Serializable
data class PluginResponse(
    val id: Uuid,
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaResponse,
    val type: String,
    val authorId: Uuid,
    val status: String,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginDetailResponse(
    val id: Uuid,
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaResponse,
    val executionConfig: PluginExecutionConfig,
    val authorId: Uuid,
    val status: String,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginSchemaResponse(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
data class PagedPluginResponse(
    val items: List<PluginResponse>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

private fun PluginExecutionConfig.typeName(): String = when (this) {
    is HttpExecutionConfig -> "HTTP"
    is KnowledgeSearchExecutionConfig -> "KNOWLEDGE_SEARCH"
}

fun Plugin.toResponse(): PluginResponse = PluginResponse(
    id = id.value,
    name = name,
    description = description,
    version = version,
    schema = PluginSchemaResponse(
        name = schema.name,
        description = schema.description,
        parameters = schema.parameters,
    ),
    type = executionConfig.typeName(),
    authorId = authorId.value,
    status = status.name.lowercase(),
    likeCount = likeCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun Plugin.toDetailResponse(): PluginDetailResponse = PluginDetailResponse(
    id = id.value,
    name = name,
    description = description,
    version = version,
    schema = PluginSchemaResponse(
        name = schema.name,
        description = schema.description,
        parameters = schema.parameters,
    ),
    executionConfig = executionConfig,
    authorId = authorId.value,
    status = status.name.lowercase(),
    likeCount = likeCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun PageResult<Plugin>.toResponse(): PagedPluginResponse = PagedPluginResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)
