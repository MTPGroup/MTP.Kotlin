package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import org.slf4j.LoggerFactory
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort
import tech.hanasaki.azusa.shared.port.out.OutboxEventRepositoryPort
import tech.hanasaki.azusa.shared.port.out.OutboxSchedulerPort


class OutboxAdapter(
    private val repository: OutboxEventRepositoryPort,
    private val eventSerializer: EventSerializerPort,
) : OutboxSchedulerPort {
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

