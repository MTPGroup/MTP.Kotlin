package tech.hanasaki.azusa.modules.chat.application.port.out.plugin

import ai.koog.agents.core.tools.Tool
import io.ktor.client.*
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.modules.plugin.domain.model.Plugin

class PluginToolFactory(
    private val httpClient: HttpClient,
) {
    fun create(plugin: Plugin, chatConfig: JsonObject? = null): Tool<*, *> =
        HttpPluginTool(plugin, httpClient, chatConfig)
}
