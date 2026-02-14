package tech.hanasaki.azusa.modules.chat.adapter.out.plugin

import io.ktor.client.*
import tech.hanasaki.azusa.modules.chat.adapter.out.llm.toToolSpecification
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolEntry
import tech.hanasaki.azusa.modules.chat.application.port.out.PluginToolFactoryPort
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin

class PluginToolFactory(
    private val httpClient: HttpClient,
) : PluginToolFactoryPort {
    override fun create(plugin: Plugin): PluginToolEntry {
        val specification = plugin.schema.toToolSpecification()
        val executor = HttpPluginTool(plugin, httpClient)
        return PluginToolEntry(specification, executor)
    }
}
