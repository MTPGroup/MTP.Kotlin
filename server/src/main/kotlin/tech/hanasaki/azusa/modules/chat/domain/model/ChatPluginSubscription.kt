package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class ChatPluginSubscriptionId(val value: Uuid)

/**
 * ChatPluginSubscription 实体 - 表示对话级别的插件订阅
 */
class ChatPluginSubscription private constructor(
    val id: ChatPluginSubscriptionId,
    val chatId: ChatId,
    val pluginId: PluginId,
    var enabled: Boolean,
    var config: JsonObject,
    val createdAt: Instant,
) : AggregateRoot() {
    companion object {
        /**
         * 创建插件订阅
         */
        fun create(
            chatId: ChatId,
            pluginId: PluginId,
            enabled: Boolean = true,
            config: JsonObject? = null,
        ): ChatPluginSubscription {
            val now = Clock.System.now()
            return ChatPluginSubscription(
                id = ChatPluginSubscriptionId(Uuid.random()),
                chatId = chatId,
                pluginId = pluginId,
                enabled = enabled,
                config = config ?: JsonObject(emptyMap()),
                createdAt = now,
            )
        }

        /**
         * 从持久化层重建订阅
         */
        fun reconstitute(
            id: ChatPluginSubscriptionId,
            chatId: ChatId,
            pluginId: PluginId,
            enabled: Boolean,
            config: JsonObject,
            createdAt: Instant,
        ): ChatPluginSubscription = ChatPluginSubscription(
            id = id,
            chatId = chatId,
            pluginId = pluginId,
            enabled = enabled,
            config = config,
            createdAt = createdAt,
        )
    }

    /**
     * 获取或创建插件订阅
     */
    fun getOrCreate(
        chatId: ChatId,
        pluginId: PluginId,
    ): ChatPluginSubscription {
        return ChatPluginSubscription(
            id = ChatPluginSubscriptionId(Uuid.random()),
            chatId = chatId,
            pluginId = pluginId,
            enabled = true,
            config = JsonObject(emptyMap()),
            createdAt = Clock.System.now(),
        )
    }

    /**
     * 启用插件
     */
    fun enable() {
        this.enabled = true
    }

    /**
     * 禁用插件
     */
    fun disable() {
        this.enabled = false
    }

    /**
     * 更新插件配置
     */
    fun updateConfig(newConfig: JsonObject) {
        this.config = newConfig
    }

    /**
     * 更新配置中的单个字段
     */
    fun updateConfigField(key: String, value: String) {
        val updatedConfig = config.toMutableMap()
        updatedConfig[key] = kotlinx.serialization.json.JsonPrimitive(value)
        this.config = JsonObject(updatedConfig)
    }
}
