package tech.hanasaki.azusa.shared.infrastructure.event.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.port.out.EventPublisherPort
import tech.hanasaki.azusa.shared.port.out.EventSerializerPort

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamEventPublisher(
    private val eventSerializer: EventSerializerPort,
    private val streamConfig: StreamConfig,
    private val redisCommands: RedisCoroutinesCommands<String, String>,
) : EventPublisherPort {
    override suspend fun publish(event: DomainEvent) {
        try {
            val message = mapOf(
                "eventId" to event.eventId.toString(),
                "eventType" to event.eventType,
                "aggregateType" to event.aggregateType,
                "aggregateId" to event.aggregateId,
                "occurredOn" to event.occurredOn.toString(),
                "payload" to eventSerializer.serialize(event)
            )

            redisCommands.xadd(streamConfig.streamKey, message)
        } catch (e: Exception) {
            throw e
        }
    }
}