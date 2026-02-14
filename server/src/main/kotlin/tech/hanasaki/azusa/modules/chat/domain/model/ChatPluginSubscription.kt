package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class ChatPluginSubscriptionId(val value: Uuid)

/**
 * ChatPluginSubscription 实体 — 嵌入 Chat 聚合根
 */
class ChatPluginSubscription(
    val id: ChatPluginSubscriptionId,
    val pluginId: PluginId,
    var enabled: Boolean,
    var config: JsonObject,
    val createdAt: Instant,
) {
    companion object {
        fun create(
            pluginId: PluginId,
            enabled: Boolean = true,
            config: JsonObject? = null,
        ): ChatPluginSubscription {
            val now = Clock.System.now()
            return ChatPluginSubscription(
                id = ChatPluginSubscriptionId(Uuid.random()),
                pluginId = pluginId,
                enabled = enabled,
                config = config ?: JsonObject(emptyMap()),
                createdAt = now,
            )
        }

        fun reconstitute(
            id: ChatPluginSubscriptionId,
            pluginId: PluginId,
            enabled: Boolean,
            config: JsonObject,
            createdAt: Instant,
        ): ChatPluginSubscription = ChatPluginSubscription(
            id = id,
            pluginId = pluginId,
            enabled = enabled,
            config = config,
            createdAt = createdAt,
        )
    }

    fun enable() {
        this.enabled = true
    }

    fun disable() {
        this.enabled = false
    }

    fun updateConfig(newConfig: JsonObject) {
        this.config = newConfig
    }

    fun updateConfigField(key: String, value: String) {
        val updatedConfig = config.toMutableMap()
        updatedConfig[key] = JsonPrimitive(value)
        this.config = JsonObject(updatedConfig)
    }
}
