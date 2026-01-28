package tech.hanasaki.azusa.common.platform.event.outbox.repository

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.platform.database.dbQuery
import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEvent
import tech.hanasaki.azusa.common.platform.event.outbox.model.OutboxEventStatus
import tech.hanasaki.azusa.common.platform.event.outbox.persistence.OutboxEventTable
import java.util.*
import kotlin.time.Instant

/**
 * Exposed 实现的 Outbox 事件仓储
 */
class ExposedOutboxEventRepository : OutboxEventRepository {

    override suspend fun save(event: OutboxEvent): Unit = dbQuery {
        OutboxEventTable.insert {
            it[id] = event.id
            it[eventType] = event.eventType
            it[status] = OutboxEventStatus.PENDING
            it[payload] = event.payload
            it[createdAt] = event.createdAt
        }
    }

    override suspend fun saveAll(events: Collection<OutboxEvent>): Unit = dbQuery {
        events.forEach { event ->
            OutboxEventTable.insert {
                it[id] = event.id
                it[eventType] = event.eventType
                it[status] = OutboxEventStatus.PENDING
                it[payload] = event.payload
                it[createdAt] = event.createdAt
            }
        }
    }

    override suspend fun findUnpublished(limit: Int): List<OutboxEvent> = dbQuery {
        OutboxEventTable
            .selectAll()
            .where {
                (OutboxEventTable.status eq OutboxEventStatus.PENDING) or
                        (OutboxEventTable.status eq OutboxEventStatus.FAILED)
            }
            .orderBy(OutboxEventTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .map { row ->
                OutboxEvent(
                    id = row[OutboxEventTable.id],
                    eventType = row[OutboxEventTable.eventType],
                    payload = row[OutboxEventTable.payload],
                    createdAt = row[OutboxEventTable.createdAt],
                )
            }
    }

    override suspend fun markAsPublished(eventId: UUID, publishedAt: Instant): Unit = dbQuery {
        OutboxEventTable.update({ OutboxEventTable.id eq eventId }) {
            it[status] = OutboxEventStatus.SENT
            it[sentAt] = publishedAt
        }
    }

    override suspend fun markAsFailed(eventId: UUID) {
        OutboxEventTable.update({ OutboxEventTable.id eq eventId }) {
            it[status] = OutboxEventStatus.FAILED
            it[retryCount] = retryCount + 1
        }
    }

    override suspend fun markAllAsPublished(eventIds: Collection<UUID>, publishedAt: Instant): Unit = dbQuery {
        if (eventIds.isEmpty()) return@dbQuery
        OutboxEventTable.update({ OutboxEventTable.id inList eventIds }) {
            it[status] = OutboxEventStatus.SENT
            it[sentAt] = publishedAt
        }
    }

    override suspend fun markAllAsFailed(eventIds: Collection<UUID>) = dbQuery {
        if (eventIds.isEmpty()) return@dbQuery
        OutboxEventTable.update({ OutboxEventTable.id inList eventIds }) {
            it[status] = OutboxEventStatus.FAILED
            it[retryCount] = retryCount + 1
        }
    }

    override suspend fun deletePublishedBefore(before: Instant): Int = dbQuery {
        OutboxEventTable.deleteWhere {
            (status eq OutboxEventStatus.SENT) and (sentAt less before)
        }
    }
}