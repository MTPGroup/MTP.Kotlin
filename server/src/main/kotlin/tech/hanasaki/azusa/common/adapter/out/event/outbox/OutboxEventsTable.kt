package tech.hanasaki.azusa.common.adapter.out.event.outbox

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object OutboxEventsTable : Table("outbox_events") {
    val id = uuid("id")
    val aggregateType = varchar("aggregate_type", 50)
    val aggregateId = varchar("aggregate_id", 50)
    val eventType = varchar("event_type", 100)
    val payload = text("payload")
    val status = enumerationByName<OutboxEventStatus>("status", 20)
    val retryCount = integer("retry_count")
    val errorMessage = text("error_message").nullable()
    val createdAt = timestamp("created_at")
    val processedAt = timestamp("processed_at").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}