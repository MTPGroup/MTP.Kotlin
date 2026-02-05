package tech.hanasaki.azusa.common.adapter.out.event.outbox

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.port.out.EventSerializer
import tech.hanasaki.azusa.common.port.out.OutboxEventRepositoryPort
import tech.hanasaki.azusa.common.port.out.OutboxScheduler


class OutboxAdapter(
    private val repository: OutboxEventRepositoryPort,
    private val eventSerializer: EventSerializer,
) : OutboxScheduler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override suspend fun schedule(event: DomainEvent) {
        val outboxEvent = OutboxEvent(
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId,
            eventType = event.eventType,
            payload = eventSerializer.serialize(event)
        )

        repository.save(outboxEvent)
    }
}

