package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.time.Clock

object PluginSubscriptionTable : Table("plugin_subscriptions") {
    val userId = uuid("user_id")
    val pluginId = uuid("plugin_id")
    val isActive = bool("is_active").default(false)
    val subscribedAt = datetime("subscribed_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))

    override val primaryKey = PrimaryKey(userId, pluginId)
}
