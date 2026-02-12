package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import kotlinx.serialization.json.*
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema
import tech.hanasaki.azusa.shared.domain.model.vo.LLMConfig
import ai.koog.prompt.llm.LLMProvider as KoogLLMProvider

/**
 * PluginSchema (domain) → ToolDescriptor (Koog)
 */
fun PluginSchema.toToolDescriptor(): ToolDescriptor {
    val properties = parameters["properties"]?.jsonObject ?: JsonObject(emptyMap())
    val requiredNames = parameters["required"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    val requiredParams = mutableListOf<ToolParameterDescriptor>()
    val optionalParams = mutableListOf<ToolParameterDescriptor>()

    for ((paramName, paramSchema) in properties) {
        val schema = paramSchema.jsonObject
        val desc = schema["description"]?.jsonPrimitive?.contentOrNull ?: ""
        val type = when (schema["type"]?.jsonPrimitive?.contentOrNull) {
            "integer", "number" -> ToolParameterType.Integer
            "boolean" -> ToolParameterType.Boolean
            "array" -> ToolParameterType.List(ToolParameterType.String)
            else -> ToolParameterType.String
        }

        val descriptor = ToolParameterDescriptor(
            name = paramName,
            description = desc,
            type = type,
        )

        if (paramName in requiredNames) {
            requiredParams.add(descriptor)
        } else {
            optionalParams.add(descriptor)
        }
    }

    return ToolDescriptor(
        name = name,
        description = description,
        requiredParameters = requiredParams,
        optionalParameters = optionalParams,
    )
}

/**
 * LLMConfig → Koog LLModel
 */
fun LLMConfig.toLLModel(): LLModel = LLModel(
    provider = KoogLLMProvider.Ollama,
    id = model,
    capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.Temperature,
        LLMCapability.Tools,
    ),
    contextLength = 262_144,
)
