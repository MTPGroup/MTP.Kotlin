package tech.hanasaki.azusa.modules.chat.application.port.out

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.service.tool.ToolExecutor
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin

data class PluginToolEntry(
    val specification: ToolSpecification,
    val executor: ToolExecutor,
)

interface PluginToolFactoryPort {
    fun create(plugin: Plugin): PluginToolEntry
}
