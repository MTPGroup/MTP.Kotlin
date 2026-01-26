package tech.hanasaki.azusa.modules.plugin.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 插件参数定义（JSON Schema 格式）
 * Agent 调用插件时使用此定义来理解工具功能和参数
 */
@Serializable
data class PluginSchema(
    /** 工具名称（Agent 调用时使用） */
    val name: String,
    /** 工具描述（Agent 决策依据） */
    val description: String,
    /** JSON Schema 参数定义 */
    val parameters: JsonObject,
)
