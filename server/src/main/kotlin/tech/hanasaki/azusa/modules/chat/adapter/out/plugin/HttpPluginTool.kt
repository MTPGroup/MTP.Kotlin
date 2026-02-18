package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.service.tool.ToolExecutor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import tech.hanasaki.azusa.modules.plugin.domain.model.HttpExecutionConfig

class HttpPluginTool(
    private val config: HttpExecutionConfig,
    private val subscriptionConfig: JsonObject,
    private val httpClient: HttpClient,
) : ToolExecutor {

    override fun execute(request: ToolExecutionRequest, memoryId: Any?): String {
        return try {
            val toolArgs = parseToolArgs(request.arguments())
            val resolvedUrl = TemplateResolver.resolve(config.urlTemplate, toolArgs, subscriptionConfig)
            val resolvedHeaders = config.headers.mapValues { (_, v) ->
                TemplateResolver.resolve(v, toolArgs, subscriptionConfig)
            }
            val resolvedBody = config.bodyTemplate?.let {
                TemplateResolver.resolve(it, toolArgs, subscriptionConfig)
            }

            runBlocking {
                val response = httpClient.request(resolvedUrl) {
                    method = HttpMethod.parse(config.method.uppercase())
                    resolvedHeaders.forEach { (k, v) -> header(k, v) }
                    if (method != HttpMethod.Get && resolvedBody != null) {
                        contentType(ContentType.Application.Json)
                        setBody(resolvedBody)
                    } else if (method != HttpMethod.Get) {
                        contentType(ContentType.Application.Json)
                        setBody(request.arguments())
                    }
                }
                val body = response.bodyAsText()
                val extracted = config.responseExtraction?.let { extractByPath(body, it) } ?: body
                if (extracted.length > config.maxResponseLength) {
                    extracted.take(config.maxResponseLength) + "...(truncated)"
                } else {
                    extracted
                }
            }
        } catch (e: Exception) {
            "Plugin execution failed: ${e.message}"
        }
    }

    private fun parseToolArgs(arguments: String): Map<String, String> {
        return try {
            val json = Json.parseToJsonElement(arguments)
            if (json is JsonObject) {
                json.mapValues { (_, v) ->
                    when (v) {
                        is JsonPrimitive -> v.content
                        else -> v.toString()
                    }
                }
            } else emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun extractByPath(jsonStr: String, path: String): String {
        return try {
            var current: JsonElement = Json.parseToJsonElement(jsonStr)
            for (key in path.split(".")) {
                current = (current as? JsonObject)?.get(key) ?: return jsonStr
            }
            when (current) {
                is JsonPrimitive -> current.content
                else -> current.toString()
            }
        } catch (_: Exception) {
            jsonStr
        }
    }
}
