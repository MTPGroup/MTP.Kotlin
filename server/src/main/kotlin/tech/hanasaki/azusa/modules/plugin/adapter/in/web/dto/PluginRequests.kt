package tech.hanasaki.azusa.modules.plugin.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema

@Serializable
data class CreatePluginRequest(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaDto,
    val executionConfig: PluginExecutionConfig,
)

@Serializable
data class UpdatePluginRequest(
    val name: String,
    val description: String,
    val version: String,
    val schema: PluginSchemaDto,
    val executionConfig: PluginExecutionConfig,
)

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
