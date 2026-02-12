package tech.hanasaki.azusa.modules.chat.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.uuid.Uuid

@Serializable
data class CreateChatRequest(val characterId: Uuid, val name: String? = null)

@Serializable
data class UpdateChatNameRequest(val name: String?)

@Serializable
data class UpdateChatConfigRequest(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topP: Double? = null,
    val systemPrompt: String? = null,
)

@Serializable
data class TogglePluginRequest(val enabled: Boolean)

@Serializable
data class UpdatePluginConfigRequest(val config: JsonObject)
