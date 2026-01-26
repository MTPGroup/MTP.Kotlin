package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.time.Clock

object PluginLikeTable : Table("plugin_likes") {
    val userId = uuid("user_id")
    val pluginId = uuid("plugin_id")
    val createdAt = datetime("created_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))

    override val primaryKey = PrimaryKey(userId, pluginId)
}
