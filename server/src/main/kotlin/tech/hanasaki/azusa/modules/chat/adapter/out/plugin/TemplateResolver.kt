package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object TemplateResolver {
    private val PLACEHOLDER_REGEX = Regex("""\{\{(\w+)}}""")

    fun resolve(
        template: String,
        toolArgs: Map<String, String>,
        subscriptionConfig: JsonObject,
    ): String = PLACEHOLDER_REGEX.replace(template) { match ->
        val key = match.groupValues[1]
        toolArgs[key]
            ?: (subscriptionConfig[key] as? JsonPrimitive)?.content
            ?: match.value
    }
}
