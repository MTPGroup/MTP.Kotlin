package tech.hanasaki.azusa.modules.plugin.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.plugin.application.command.CreatePluginCommand
import tech.hanasaki.azusa.modules.plugin.application.command.UpdatePluginCommand
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema

@Serializable
data class CreatePluginRequest(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaDto,
    val code: String,
) {
    fun toCommand(): CreatePluginCommand = CreatePluginCommand(
        name = name,
        description = description,
        version = version,
        schema = schema.toDomain(),
        code = code,
    )
}

@Serializable
data class UpdatePluginRequest(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaDto,
    val code: String,
) {
    fun toCommand(): UpdatePluginCommand = UpdatePluginCommand(
        name = name,
        description = description,
        version = version,
        schema = schema.toDomain(),
        code = code,
    )
}

@Serializable
data class PluginSchemaDto(
    val name: String,
    val description: String,
    val parameters: JsonObject,
) {
    fun toDomain(): PluginSchema = PluginSchema(
        name = name,
        description = description,
        parameters = parameters,
    )
}
