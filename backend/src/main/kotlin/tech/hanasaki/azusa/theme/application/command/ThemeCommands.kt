package tech.hanasaki.azusa.theme.application.command

import tech.hanasaki.azusa.shared.ThemeId
import tech.hanasaki.azusa.theme.domain.model.ThemeDefinition

data class CreateThemeCommand(
    val id: ThemeId? = null,
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinition,
    val version: String,
)

data class UpdateThemeCommand(
    val name: String,
    val description: String?,
    val previewUrl: String?,
    val data: ThemeDefinition,
    val version: String,
)
