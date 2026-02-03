package tech.hanasaki.azusa.modules.theme.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.theme.application.command.CreateThemeCommand
import tech.hanasaki.azusa.modules.theme.application.command.UpdateThemeCommand
import tech.hanasaki.azusa.modules.theme.domain.model.ThemeDefinition
import tech.hanasaki.azusa.common.kernel.model.ThemeId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ThemeDefinitionRequest(
    val lightColors: Map<String, String>,
    val darkColors: Map<String, String>,
    val roundness: Int = 4,
) {
    fun toDomain(): ThemeDefinition = ThemeDefinition(
        lightColors = lightColors,
        darkColors = darkColors,
        roundness = roundness,
    )
}


@Serializable
data class CreateThemeRequest(
    @Contextual val id: Uuid? = null,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinitionRequest,
    val version: String,
) {
    fun toCommand(): CreateThemeCommand = CreateThemeCommand(
        id = id?.let { ThemeId(it) },
        name = name,
        description = description,
        previewUrl = previewUrl,
        data = data.toDomain(),
        version = version,
    )
}

@Serializable
data class UpdateThemeRequest(
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinitionRequest,
    val version: String,
) {
    fun toCommand(): UpdateThemeCommand = UpdateThemeCommand(
        name = name,
        description = description,
        previewUrl = previewUrl,
        data = data.toDomain(),
        version = version,
    )
}
