package tech.hanasaki.azusa.modules.plugin.application.command

import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema

data class CreatePluginCommand(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchema,
    val code: String,
)

data class UpdatePluginCommand(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchema,
    val code: String,
)
