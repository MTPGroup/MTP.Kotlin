package tech.hanasaki.azusa.common.adapter.out.event.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.port.out.EventPublisher
import tech.hanasaki.azusa.common.port.out.EventSerializer

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisStreamEventPublisher(
    private val eventSerializer: EventSerializer,
    private val streamConfig: StreamConfig,
    private val redisCommands: RedisCoroutinesCommands<String, String>,
) : EventPublisher {
    override suspend fun publish(event: DomainEvent) {
        val lock = LettuceDistLock(
            redisCommands,
            "azusa:lock"
        )

        if (lock.tryLock()) {
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
            } finally {
                lock.unlock()
            }
        }

    }

    override suspend fun publishAll(events: Collection<DomainEvent>) {
        TODO("Not yet implemented")
    }
}