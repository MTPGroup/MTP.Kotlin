package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.port.out.OutboxEventRepositoryPort
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Exposed 实现的 Outbox 事件仓储
 */

class ExposedOutboxEventRepository : OutboxEventRepositoryPort {

    override suspend fun save(event: OutboxEvent) {
        OutboxEventsTable.insert {
            it[id] = event.id
            toEntity(event, it)
        }
    }

    override suspend fun saveAll(events: Collection<OutboxEvent>) {
        events.forEach { event ->
            OutboxEventsTable.insert {
                it[id] = event.id
                toEntity(event, it)
            }
        }
    }

    override suspend fun findPending(limit: Int): List<OutboxEvent> {
        return OutboxEventsTable
            .selectAll()
            .where {
                (OutboxEventsTable.status eq OutboxEventStatus.PENDING)
            }
            .orderBy(OutboxEventsTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .map(::toDomain)
    }

    override suspend fun markAsSent(id: Uuid) {
        OutboxEventsTable.update({ OutboxEventsTable.id eq id }) {
            it[status] = OutboxEventStatus.SENT
            it[processedAt] = Clock.System.now()
        }
    }

    override suspend fun markAsFailed(id: Uuid, error: String) {
        OutboxEventsTable.update({ OutboxEventsTable.id eq id }) {
            it[status] = OutboxEventStatus.FAILED
            it[errorMessage] = error
            it[retryCount] = retryCount + 1
        }
    }

    override suspend fun markAllAsPublished(ids: Collection<Uuid>) {
        if (ids.isEmpty()) return
        OutboxEventsTable.update({ OutboxEventsTable.id inList ids }) {
            it[status] = OutboxEventStatus.SENT
            it[processedAt] = Clock.System.now()
        }
    }

    override suspend fun markAllAsFailed(ids: Collection<Uuid>) {
        if (ids.isEmpty()) return
        OutboxEventsTable.update({ OutboxEventsTable.id inList ids }) {
            it[status] = OutboxEventStatus.FAILED
            it[retryCount] = retryCount + 1
        }
    }

    override suspend fun deletePublishedBefore(before: Instant): Int {
        return OutboxEventsTable.deleteWhere {
            (status eq OutboxEventStatus.SENT) and (processedAt less before)
        }
    }

    private fun toEntity(domain: OutboxEvent, target: UpdateBuilder<*>) {
        target[OutboxEventsTable.aggregateType] = domain.aggregateType
        target[OutboxEventsTable.aggregateId] = domain.aggregateId
        target[OutboxEventsTable.eventType] = domain.eventType
        target[OutboxEventsTable.status] = domain.status
        target[OutboxEventsTable.payload] = domain.payload
        target[OutboxEventsTable.createdAt] = domain.createdAt
    }

    private fun toDomain(row: ResultRow): OutboxEvent = OutboxEvent(
        id = row[OutboxEventsTable.id],
        aggregateType = row[OutboxEventsTable.aggregateType],
        aggregateId = row[OutboxEventsTable.aggregateId],
        eventType = row[OutboxEventsTable.eventType],
        payload = row[OutboxEventsTable.payload],
        status = row[OutboxEventsTable.status],
        retryCount = row[OutboxEventsTable.retryCount],
        createdAt = row[OutboxEventsTable.createdAt],
    )
}