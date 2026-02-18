package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.service.tool.ToolExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.modules.plugin.domain.model.KnowledgeSearchExecutionConfig
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class KnowledgeSearchPluginTool(
    private val config: KnowledgeSearchExecutionConfig,
    private val knowledgeSearcher: KnowledgeSearcherPort,
    private val userId: UserId,
) : ToolExecutor {

    override fun execute(request: ToolExecutionRequest, memoryId: Any?): String {
        return try {
            val query = extractQuery(request.arguments())
            runBlocking {
                val results = knowledgeSearcher.search(
                    userId = userId,
                    knowledgeBaseIds = config.knowledgeBaseIds,
                    query = query,
                    limit = config.limit,
                )
                if (results.isEmpty()) {
                    "No results found."
                } else {
                    results.joinToString("\n\n") { result ->
                        buildString {
                            append(result.content)
                            if (result.source != null) {
                                append("\n[Source: ${result.source}]")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            "Knowledge search failed: ${e.message}"
        }
    }

    private fun extractQuery(arguments: String): String {
        return try {
            val json = Json.parseToJsonElement(arguments)
            if (json is JsonObject) {
                (json["query"] as? JsonPrimitive)?.content ?: arguments
            } else {
                arguments
            }
        } catch (_: Exception) {
            arguments
        }
    }
}
