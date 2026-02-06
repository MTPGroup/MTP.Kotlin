package tech.hanasaki.azusa.shared.infrastructure.event.outbox

import tech.hanasaki.azusa.shared.domain.event.IntegrationEvent
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort
import tech.hanasaki.azusa.shared.port.out.OutboxEventRepositoryPort
import tech.hanasaki.azusa.shared.port.out.OutboxSchedulerPort

class OutboxAdapter(
    private val repository: OutboxEventRepositoryPort,
    private val eventSerializer: EventSerializerPort,
) : OutboxSchedulerPort {

    override suspend fun schedule(event: IntegrationEvent) {
        val outboxEvent = OutboxEvent(
            eventType = event.eventType,
            payload = eventSerializer.serialize(event)
        )
        repository.save(outboxEvent)
    }
}
