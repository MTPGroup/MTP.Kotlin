package tech.hanasaki.azusa.modules.chat.application.port.out.plugin

import ai.koog.agents.core.tools.Tool
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.toToolDescriptor
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin

class HttpPluginTool(
    private val plugin: Plugin,
    private val httpClient: HttpClient,
    private val chatConfig: JsonObject?,
) : Tool<JsonObject, String>(
    argsSerializer = JsonObject.serializer(),
    resultSerializer = serializer<String>(),
    descriptor = plugin.schema.toToolDescriptor(),
) {
    companion object {
        private const val MAX_RESPONSE_LENGTH = 8000
    }

    override suspend fun execute(args: JsonObject): String {
        return try {
            val response = httpClient.post(plugin.code) {
                contentType(ContentType.Application.Json)
                setBody(args.toString())
            }
            val body = response.bodyAsText()
            if (body.length > MAX_RESPONSE_LENGTH) body.take(MAX_RESPONSE_LENGTH) + "...(truncated)" else body
        } catch (e: Exception) {
            "Plugin execution failed: ${e.message}"
        }
    }
}
