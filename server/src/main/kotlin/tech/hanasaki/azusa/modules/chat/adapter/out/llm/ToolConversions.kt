package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.model.chat.request.json.*
import kotlinx.serialization.json.*
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema

/**
 * PluginSchema (domain) -> ToolSpecification (LangChain4j)
 */
fun PluginSchema.toToolSpecification(): ToolSpecification {
    val properties = parameters["properties"]?.jsonObject ?: JsonObject(emptyMap())
    val requiredNames = parameters["required"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    val schemaBuilder = JsonObjectSchema.builder()

    for ((paramName, paramSchema) in properties) {
        val schema = paramSchema.jsonObject
        val desc = schema["description"]?.jsonPrimitive?.contentOrNull ?: ""
        val jsonSchema: JsonSchemaElement = when (schema["type"]?.jsonPrimitive?.contentOrNull) {
            "integer" -> JsonIntegerSchema.builder().description(desc).build()
            "number" -> JsonNumberSchema.builder().description(desc).build()
            "boolean" -> JsonBooleanSchema.builder().description(desc).build()
            "array" -> JsonArraySchema.builder()
                .description(desc)
                .items(JsonStringSchema.builder().build())
                .build()
            else -> JsonStringSchema.builder().description(desc).build()
        }
        schemaBuilder.addProperty(paramName, jsonSchema)
    }

    schemaBuilder.required(requiredNames)

    return ToolSpecification.builder()
        .name(name)
        .description(description)
        .parameters(schemaBuilder.build())
        .build()
}
