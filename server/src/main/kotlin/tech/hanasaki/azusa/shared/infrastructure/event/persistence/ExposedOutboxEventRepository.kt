package tech.hanasaki.azusa.shared.infrastructure.event.persistence

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.event.OutboxEvent
import tech.hanasaki.azusa.shared.domain.event.OutboxEventRepository
import tech.hanasaki.azusa.shared.infrastructure.database.OutboxEventTable
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery
import java.util.UUID
import kotlin.time.Instant

/**
 * Exposed 实现的 Outbox 事件仓储
 */
class ExposedOutboxEventRepository : OutboxEventRepository {

    override suspend fun save(event: OutboxEvent): Unit = dbQuery {
        OutboxEventTable.insert {
            it[id] = event.id
            it[eventType] = event.eventType
            it[serializedEvent] = event.payload
            it[publicationDate] = event.occurredAt
            it[completionDate] = event.publishedAt
        }
    }

    override suspend fun saveAll(events: Collection<OutboxEvent>): Unit = dbQuery {
        events.forEach { event ->
            OutboxEventTable.insert {
                it[id] = event.id
                it[eventType] = event.eventType
                it[serializedEvent] = event.payload
                it[publicationDate] = event.occurredAt
                it[completionDate] = event.publishedAt
            }
        }
    }

    override suspend fun findUnpublished(limit: Int): List<OutboxEvent> = dbQuery {
        OutboxEventTable
            .selectAll()
            .where { OutboxEventTable.completionDate.isNull() }
            .orderBy(OutboxEventTable.publicationDate, SortOrder.ASC)
            .limit(limit)
            .map { row ->
                OutboxEvent(
                    id = row[OutboxEventTable.id],
                    eventType = row[OutboxEventTable.eventType],
                    payload = row[OutboxEventTable.serializedEvent],
                    occurredAt = row[OutboxEventTable.publicationDate],
                    publishedAt = row[OutboxEventTable.completionDate],
                )
            }
    }

    override suspend fun markAsPublished(eventId: UUID, publishedAt: Instant): Unit = dbQuery {
        OutboxEventTable.update({ OutboxEventTable.id eq eventId }) {
            it[completionDate] = publishedAt
        }
    }

    override suspend fun markAllAsPublished(eventIds: Collection<UUID>, publishedAt: Instant): Unit = dbQuery {
        if (eventIds.isEmpty()) return@dbQuery
        OutboxEventTable.update({ OutboxEventTable.id inList eventIds }) {
            it[completionDate] = publishedAt
        }
    }

    override suspend fun deletePublishedBefore(before: Instant): Int = dbQuery {
        OutboxEventTable.deleteWhere {
            (completionDate.isNotNull()) and (completionDate less before)
        }
    }
}
