package tech.hanasaki.azusa.modules.theme.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.theme.domain.model.Theme
import tech.hanasaki.azusa.modules.theme.domain.model.ThemeDefinition
import java.util.*

@Serializable
data class ThemeDefinitionResponse(
    val lightColors: Map<String, String>,
    val darkColors: Map<String, String>,
    val roundness: Int = 4,
)

@Serializable
data class ThemeResponse(
    @Contextual val id: UUID,
    @Contextual val authorId: UUID,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinitionResponse,
    val downloadCount: Int,
    val version: String,
    val createdAt: String,
    val updatedAt: String,
)

fun ThemeDefinition.toResponse(): ThemeDefinitionResponse = ThemeDefinitionResponse(
    lightColors = lightColors,
    darkColors = darkColors,
    roundness = roundness,
)

fun Theme.toResponse(): ThemeResponse = ThemeResponse(
    id = id.value,
    authorId = authorId.value,
    name = name,
    description = description,
    previewUrl = previewUrl,
    data = data.toResponse(),
    downloadCount = downloadCount,
    version = version,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
