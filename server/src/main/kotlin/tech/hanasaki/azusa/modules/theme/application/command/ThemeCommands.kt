package tech.hanasaki.azusa.modules.theme.application.command

import tech.hanasaki.azusa.modules.theme.domain.model.ThemeDefinition
import tech.hanasaki.azusa.common.kernel.model.ThemeId

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
