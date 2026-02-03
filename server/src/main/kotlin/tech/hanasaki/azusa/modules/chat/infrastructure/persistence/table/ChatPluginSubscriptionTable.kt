package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.Clock

object ChatPluginSubscriptionTable : Table("chat_plugin_subscriptions") {
    val id = uuid("id")
    val chatId = uuid("chat_id")
    val pluginId = uuid("plugin_id")
    val enabled = bool("enabled").default(true)
    val config = jsonb<JsonObject>("config", Json { prettyPrint = true }).default(JsonObject(emptyMap()))
    val createdAt = timestamp("created_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(chatId, pluginId)
    }
}
