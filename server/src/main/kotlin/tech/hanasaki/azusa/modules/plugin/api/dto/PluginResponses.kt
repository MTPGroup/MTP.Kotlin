package tech.hanasaki.azusa.modules.plugin.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import kotlin.uuid.Uuid


@Serializable
data class PluginResponse(
    @Contextual val id: Uuid,
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaResponse,
    @Contextual val authorId: Uuid,
    val status: String,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PluginDetailResponse(
    @Contextual val id: Uuid,
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaResponse,
    val code: String,
    @Contextual val authorId: Uuid,
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
    code = code,
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


