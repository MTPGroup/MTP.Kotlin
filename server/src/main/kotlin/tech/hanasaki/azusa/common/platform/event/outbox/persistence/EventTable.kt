package tech.hanasaki.azusa.common.platform.event.outbox.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Outbox 事件表 - 使用现有的 event_publication 表
 */
object OutboxEventTable : Table("event_publication") {
    val id = uuid("id")
    val eventType = varchar("event_type", 512)
    val listenerId = varchar("listener_id", 512).default("outbox")
    val serializedEvent = text("serialized_event")
    val publicationDate = timestamp("publication_date")
    val completionDate = timestamp("completion_date").nullable()

    override val primaryKey = PrimaryKey(id)
}
