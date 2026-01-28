package tech.hanasaki.azusa.common.platform.event.outbox.persistence

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEventStatus

/**
 * Outbox 事件表 - 使用现有的 event_publication 表
 */
object OutboxEventTable : Table("outbox_events") {
    val id = uuid("id")
    val eventType = varchar("type", 512)
    val status = enumerationByName<OutboxEventStatus>("status", 20)
    val payload = jsonb<JsonElement>("payload", Json.Default)
    val retryCount = integer("retry_count")
    val createdAt = timestamp("created_at")
    val sentAt = timestamp("sent_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
