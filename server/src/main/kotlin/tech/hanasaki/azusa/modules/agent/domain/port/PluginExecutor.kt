package tech.hanasaki.azusa.modules.agent.domain.port

import kotlinx.serialization.json.JsonObject

/**
 * 插件执行器端口接口
 * 用于执行已订阅的插件工具
 */
interface PluginExecutor {
    /**
     * 执行插件
     *
     * @param pluginName 插件名称
     * @param arguments 插件执行参数
     * @return 插件执行结果
     */
    suspend fun execute(pluginName: String, arguments: JsonObject): String
}
