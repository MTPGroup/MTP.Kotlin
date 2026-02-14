package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.service.tool.ToolExecutor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin

class HttpPluginTool(
    private val plugin: Plugin,
    private val httpClient: HttpClient,
) : ToolExecutor {
    companion object {
        private const val MAX_RESPONSE_LENGTH = 8000
    }

    override fun execute(request: ToolExecutionRequest, memoryId: Any?): String {
        return try {
            runBlocking {
                val response = httpClient.post(plugin.code) {
                    contentType(ContentType.Application.Json)
                    setBody(request.arguments())
                }
                val body = response.bodyAsText()
                if (body.length > MAX_RESPONSE_LENGTH) body.take(MAX_RESPONSE_LENGTH) + "...(truncated)" else body
            }
        } catch (e: Exception) {
            "Plugin execution failed: ${e.message}"
        }
    }
}
