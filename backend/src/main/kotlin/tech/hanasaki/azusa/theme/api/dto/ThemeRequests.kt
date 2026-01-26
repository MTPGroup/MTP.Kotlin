package tech.hanasaki.azusa.theme.api.dto

import tech.hanasaki.azusa.common.ThemeId
import tech.hanasaki.azusa.theme.application.command.CreateThemeCommand
import tech.hanasaki.azusa.theme.application.command.UpdateThemeCommand
import tech.hanasaki.azusa.theme.domain.model.ThemeDefinition

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

data class CreateThemeRequest(
    val id: ThemeId? = null,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinitionRequest,
    val version: String,
) {
    fun toCommand(): CreateThemeCommand = CreateThemeCommand(
        id = id,
        name = name,
        description = description,
        previewUrl = previewUrl,
        data = data.toDomain(),
        version = version,
    )
}

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
