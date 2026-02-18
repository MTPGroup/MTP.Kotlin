package tech.hanasaki.azusa.modules.plugin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PluginExecutionConfig

@Serializable
@SerialName("HTTP")
data class HttpExecutionConfig(
    val method: String = "POST",
    val urlTemplate: String,
    val headers: Map<String, String> = emptyMap(),
    val bodyTemplate: String? = null,
    val responseExtraction: String? = null,
    val maxResponseLength: Int = 8000,
) : PluginExecutionConfig()

@Serializable
@SerialName("KNOWLEDGE_SEARCH")
data class KnowledgeSearchExecutionConfig(
    val knowledgeBaseIds: List<String>,
    val limit: Int = 5,
) : PluginExecutionConfig()
