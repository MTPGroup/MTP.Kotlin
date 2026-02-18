package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import io.ktor.client.*
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.toToolSpecification
import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolEntry
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolFactoryPort
import tech.hanasaki.azusa.modules.plugin.domain.model.HttpExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.KnowledgeSearchExecutionConfig
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class PluginToolFactory(
    private val httpClient: HttpClient,
    private val knowledgeSearcher: KnowledgeSearcherPort,
) : PluginToolFactoryPort {
    override fun create(plugin: Plugin, subscriptionConfig: JsonObject, userId: UserId): PluginToolEntry {
        val specification = plugin.schema.toToolSpecification()
        val executor = when (val config = plugin.executionConfig) {
            is HttpExecutionConfig -> HttpPluginTool(config, subscriptionConfig, httpClient)
            is KnowledgeSearchExecutionConfig -> KnowledgeSearchPluginTool(config, knowledgeSearcher, userId)
        }
        return PluginToolEntry(specification, executor)
    }
}
